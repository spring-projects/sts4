/*******************************************************************************
 * Copyright (c) 2017, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.cache.IndexCache;
import org.springframework.ide.vscode.boot.index.cache.IndexCacheKey;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.beans.CachedBean;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.boot.java.reconcilers.CachedDiagnostics;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.PercentageProgressTask;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonObject;

/**
 * @author Martin Lippert
 */
public class SpringIndexerJava implements SpringIndexer {
	
	public static enum SCAN_PASS {
		ONE, TWO
	}

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	// whenever the implementation of the indexer changes in a way that the stored data in the cache is no longer valid,
	// we need to change the generation - this will result in a re-indexing due to no up-to-date cache data being found
	private static final String GENERATION = "GEN-12";
	private static final String INDEX_FILES_TASK_ID = "index-java-source-files-task-";

	private static final String SYMBOL_KEY = "symbols";
	private static final String BEANS_KEY = "beans";
	private static final String DIAGNOSTICS_KEY = "diagnostics";

	private final SymbolHandler symbolHandler;
	private final AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private final JdtReconciler reconciler;
	private final IndexCache cache;
	private final JavaProjectFinder projectFinder;
	private final ProgressService progressService;
	private final CompilationUnitCache cuCache;

	private boolean scanTestJavaSources = false;
	private JsonObject validationSeveritySettings;
	private int scanChunkSize = 1000;

	private FileScanListener fileScanListener = null; //used by test code only

	private final SpringIndexerJavaDependencyTracker dependencyTracker = new SpringIndexerJavaDependencyTracker();
	private final BiFunction<AtomicReference<TextDocument>, BiConsumer<String, Diagnostic>, IProblemCollector> problemCollectorCreator;

	private final Set<String> jakartaAnnnotations = Set.of(
			Annotations.RESOURCE_JAKARTA, Annotations.INJECT_JAKARTA, Annotations.NAMED_JAKARTA,
			Annotations.RESOURCE_JAVAX, Annotations.INJECT_JAVAX, Annotations.NAMED_JAVAX
	);

	
	public SpringIndexerJava(SymbolHandler symbolHandler, AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders, IndexCache cache,
			JavaProjectFinder projectFimder, ProgressService progressService, JdtReconciler jdtReconciler,
			BiFunction<AtomicReference<TextDocument>, BiConsumer<String, Diagnostic>, IProblemCollector> problemCollectorCreator,
			JsonObject validationSeveritySettings, CompilationUnitCache cuCache) {
		this.symbolHandler = symbolHandler;
		this.symbolProviders = symbolProviders;
		this.reconciler = jdtReconciler;
		this.cache = cache;
		this.projectFinder = projectFimder;
		this.progressService = progressService;
		
		this.problemCollectorCreator = problemCollectorCreator;
		this.validationSeveritySettings = validationSeveritySettings;
		this.cuCache = cuCache;
	}

	public SpringIndexerJavaDependencyTracker getDependencyTracker() {
		return dependencyTracker;
	}
	
	@Override
	public String[] getFileWatchPatterns() {
		return new String[] {"**/*.java"};
	}

	@Override
	public boolean isInterestedIn(String resource) {
		return resource.endsWith(".java") && !resource.endsWith("package-info.java");
	}

	@Override
	public void initializeProject(IJavaProject project, boolean clean) throws Exception {
		String[] files = this.getFiles(project);

		log.info("scan java files for symbols for project: {} - no. of files: {}", project.getElementName(), files.length);

		long startTime = System.currentTimeMillis();
		scanFiles(project, files, clean);
		long endTime = System.currentTimeMillis();

		log.info("scan java files for symbols for project: {} took ms: {}", project.getElementName(), endTime - startTime);
	}

	@Override
	public void removeProject(IJavaProject project) throws Exception {
		IndexCacheKey symbolsCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		this.cache.remove(symbolsCacheKey);
		this.cache.remove(beansCacheKey);
		this.cache.remove(diagnosticsCacheKey);
	}

	@Override
	public void updateFile(IJavaProject project, DocumentDescriptor updatedDoc, String content) throws Exception {
		IndexCacheKey symbolCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		if (updatedDoc != null && shouldProcessDocument(project, updatedDoc.getDocURI())) {
			if (isCacheOutdated(symbolCacheKey, updatedDoc.getDocURI(), updatedDoc.getLastModified())
					|| isCacheOutdated(beansCacheKey, updatedDoc.getDocURI(), updatedDoc.getLastModified())
					|| isCacheOutdated(diagnosticsCacheKey, updatedDoc.getDocURI(), updatedDoc.getLastModified())) {

				this.symbolHandler.removeSymbols(project, updatedDoc.getDocURI());
				scanFile(project, updatedDoc, content);
			}
		}
	}
	
	@Override
	public void updateFiles(IJavaProject project, DocumentDescriptor[] updatedDocs) throws Exception {
		if (updatedDocs != null) {
			DocumentDescriptor[] docs = filterDocuments(project, updatedDocs);

			if (docs.length > 0) {
				for (DocumentDescriptor updatedDoc : docs) {
					this.symbolHandler.removeSymbols(project, updatedDoc.getDocURI());
				}
				scanFiles(project, docs);
			}
		}
	}
	
	@Override
	public void removeFiles(IJavaProject project, String[] docURIs) throws Exception {
		
		String[] files = Arrays.stream(docURIs).map(docURI -> {
			try {
				return new File(new URI(docURI)).getAbsolutePath();
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			}
		}).toArray(String[]::new);
		
		IndexCacheKey symbolsCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		this.cache.removeFiles(symbolsCacheKey, files, CachedSymbol.class);
		this.cache.removeFiles(beansCacheKey, files, CachedBean.class);
		this.cache.removeFiles(diagnosticsCacheKey, files, CachedDiagnostics.class);
	}

	private DocumentDescriptor[] filterDocuments(IJavaProject project, DocumentDescriptor[] updatedDocs) {
		IndexCacheKey symbolsCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		return Arrays.stream(updatedDocs).filter(doc -> shouldProcessDocument(project, doc.getDocURI()))
				.filter(doc -> isCacheOutdated(symbolsCacheKey, doc.getDocURI(), doc.getLastModified())
						|| isCacheOutdated(beansCacheKey, doc.getDocURI(), doc.getLastModified())
						|| isCacheOutdated(diagnosticsCacheKey, doc.getDocURI(), doc.getLastModified()))
				.toArray(DocumentDescriptor[]::new);
	}

	private boolean shouldProcessDocument(IJavaProject project, String docURI) {
		try {
			Path path = new File(new URI(docURI)).toPath();
			return foldersToScan(project)
					.filter(sourceFolder -> path.startsWith(sourceFolder.toPath()))
					.findFirst()
					.isPresent();
		} catch (URISyntaxException e) {
			log.info("shouldProcessDocument - docURI syntax problem: {}", docURI);
			return false;
		}
	}
	
	private boolean isCacheOutdated(IndexCacheKey cacheKey, String docURI, long modifiedTimestamp) {
		long cachedModificationTImestamp = this.cache.getModificationTimestamp(cacheKey, UriUtil.toFileString(docURI));
		return modifiedTimestamp > cachedModificationTImestamp;
	}

	private void scanFiles(IJavaProject project, DocumentDescriptor[] docs) throws Exception {
		Set<String> scannedFiles = new HashSet<>();
		for (int i = 0; i < docs.length; i++) {
			String file = UriUtil.toFileString(docs[i].getDocURI());
			scannedFiles.add(file);
		}
		
		Set<String> scannedTypes = scanFilesInternally(project, docs);
		scanAffectedFiles(project, scannedTypes, scannedFiles);
	}

	private void scanFile(IJavaProject project, DocumentDescriptor updatedDoc, String content) throws Exception {
		final boolean ignoreMethodBodies = false;
		ASTParserCleanupEnabled parser = createParser(project, new AnnotationHierarchies(), ignoreMethodBodies);
		
		String docURI = updatedDoc.getDocURI();
		long lastModified = updatedDoc.getLastModified();
		
		if (content == null) {
			Path path = new File(new URI(docURI)).toPath();
			content = new String(Files.readAllBytes(path));
		}
		
		String unitName = docURI.substring(docURI.lastIndexOf("/") + 1); // skip over '/'
		parser.setUnitName(unitName);
		log.debug("Scan file: {}", unitName);
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu != null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
			List<CachedBean> generatedBeans = new ArrayList<CachedBean>();
			List<CachedDiagnostics> generatedDiagnostics = new ArrayList<CachedDiagnostics>();
			
			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			String file = UriUtil.toFileString(docURI);
			
			BiConsumer<String, Diagnostic> diagnosticsAggregator = new BiConsumer<>() {
				@Override
				public void accept(String docURI, Diagnostic diagnostic) {
					generatedDiagnostics.add(new CachedDiagnostics(docURI, diagnostic));
				}
			};
			
			IProblemCollector problemCollector = problemCollectorCreator.apply(docRef, diagnosticsAggregator);

			SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, file,
					lastModified, docRef, content, generatedSymbols, generatedBeans, problemCollector, SCAN_PASS.ONE, new ArrayList<>(), !ignoreMethodBodies);

			scanAST(context, true);

			IndexCacheKey symbolCacheKey = getCacheKey(project, SYMBOL_KEY);
			IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
			IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);
			
			this.cache.update(symbolCacheKey, file, lastModified, generatedSymbols, context.getDependencies(), CachedSymbol.class);
			this.cache.update(beansCacheKey, file, lastModified, generatedBeans, context.getDependencies(), CachedBean.class);
			this.cache.update(diagnosticsCacheKey, file, lastModified, generatedDiagnostics, context.getDependencies(), CachedDiagnostics.class);
//			dependencyTracker.dump();

			EnhancedSymbolInformation[] symbols = generatedSymbols.stream().map(cachedSymbol -> cachedSymbol.getEnhancedSymbol()).toArray(EnhancedSymbolInformation[]::new);
			Bean[] beans = generatedBeans.stream().filter(cachedBean -> cachedBean.getBean() != null).map(cachedBean -> cachedBean.getBean()).toArray(Bean[]::new);
			List<Diagnostic> diagnostics = generatedDiagnostics.stream().filter(cachedDiagnostics -> cachedDiagnostics.getDiagnostic() != null).map(cachedDiagnostic -> cachedDiagnostic.getDiagnostic()).collect(Collectors.toList());

			symbolHandler.addSymbols(project, docURI, symbols, beans, diagnostics);
			
			Set<String> scannedFiles = new HashSet<>();
			scannedFiles.add(file);
			fileScannedEvent(file);
			scanAffectedFiles(project, context.getScannedTypes(), scannedFiles);
		}
	}
	
	public List<EnhancedSymbolInformation> computeSymbols(IJavaProject project, String docURI, String content) throws Exception {
		if (content != null) {
			URI uri = URI.create(docURI);
			
			return cuCache.withCompilationUnit(project, uri, cu -> {
				List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
				List<CachedBean> generatedBeans = new ArrayList<CachedBean>();

				IProblemCollector voidProblemCollector = new IProblemCollector() {
					@Override
					public void endCollecting() {
					}

					@Override
					public void beginCollecting() {
					}

					@Override
					public void accept(ReconcileProblem problem) {
					}
				};

				AtomicReference<TextDocument> docRef = new AtomicReference<>();
				String file = UriUtil.toFileString(docURI);
				SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, file,
						0, docRef, content, generatedSymbols, generatedBeans, voidProblemCollector, SCAN_PASS.ONE, new ArrayList<>(), true);

				scanAST(context, false);

				return generatedSymbols.stream().map(s -> s.getEnhancedSymbol()).collect(Collectors.toList());
			});
		}
		
		return Collections.emptyList();
	}

	private Set<String> scanFilesInternally(IJavaProject project, DocumentDescriptor[] docs) throws Exception {
		final boolean ignoreMethodBodies = false;
		
		// this is to keep track of already scanned files to avoid endless loops due to circular dependencies
		Set<String> scannedTypes = new HashSet<>();

		Map<String, DocumentDescriptor> updatedDocs = new HashMap<>(); // docURI -> UpdatedDoc
		String[] javaFiles = new String[docs.length];
		long[] lastModified = new long[docs.length];

		for (int i = 0; i < docs.length; i++) {
			updatedDocs.put(docs[i].getDocURI(), docs[i]);

			String file = UriUtil.toFileString(docs[i].getDocURI());

			javaFiles[i] = file;
			lastModified[i] = docs[i].getLastModified();
		}
		
		List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
		List<CachedBean> generatedBeans = new ArrayList<CachedBean>();
		List<CachedDiagnostics> generatedDiagnostics = new ArrayList<CachedDiagnostics>();

		Multimap<String, String> dependencies = MultimapBuilder.hashKeys().hashSetValues().build();
		
		BiConsumer<String, Diagnostic> diagnosticsAggregator = new BiConsumer<>() {
			@Override
			public void accept(String docURI, Diagnostic diagnostic) {
				generatedDiagnostics.add(new CachedDiagnostics(docURI, diagnostic));
			}
		};

		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				File file = new File(sourceFilePath);
				String docURI = UriUtil.toUri(file).toASCIIString();
				
				DocumentDescriptor updatedDoc = updatedDocs.get(docURI);
				long lastModified = updatedDoc.getLastModified();
				
				AtomicReference<TextDocument> docRef = new AtomicReference<>();

				IProblemCollector problemCollector = problemCollectorCreator.apply(docRef, diagnosticsAggregator);

				SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, sourceFilePath,
						lastModified, docRef, null, generatedSymbols, generatedBeans, problemCollector, SCAN_PASS.ONE, new ArrayList<>(), !ignoreMethodBodies);
				
				scanAST(context, true);
				
				dependencies.putAll(sourceFilePath, context.getDependencies());
				scannedTypes.addAll(context.getScannedTypes());

				fileScannedEvent(sourceFilePath);
			}
		};

		AnnotationHierarchies annotationHierarchies = new AnnotationHierarchies();
		List<String[]> chunks = createChunks(javaFiles, this.scanChunkSize);
		for(int i = 0; i < chunks.size(); i++) {
			log.info("scan java files, AST parse, chunk {} for files: {}", i, javaFiles.length);
			
			ASTParserCleanupEnabled parser = createParser(project, annotationHierarchies, ignoreMethodBodies);
			parser.createASTs(chunks.get(i), null, new String[0], requestor, null);
			parser.cleanup();
		}
		
		EnhancedSymbolInformation[] symbols = generatedSymbols.stream().map(cachedSymbol -> cachedSymbol.getEnhancedSymbol()).toArray(EnhancedSymbolInformation[]::new);
		Bean[] beans = generatedBeans.stream().filter(cachedBean -> cachedBean.getBean() != null).map(cachedBean -> cachedBean.getBean()).toArray(Bean[]::new);
		Map<String, List<Diagnostic>> diagnosticsByDoc = generatedDiagnostics.stream().filter(cachedDiagnostic -> cachedDiagnostic.getDiagnostic() != null).collect(Collectors.groupingBy(CachedDiagnostics::getDocURI, Collectors.mapping(CachedDiagnostics::getDiagnostic, Collectors.toList())));
		addEmptyDiagnostics(diagnosticsByDoc, javaFiles);
		symbolHandler.addSymbols(project, symbols, beans, diagnosticsByDoc);

		IndexCacheKey symbolsCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		this.cache.update(symbolsCacheKey, javaFiles, lastModified, generatedSymbols, dependencies, CachedSymbol.class);
		this.cache.update(beansCacheKey, javaFiles, lastModified, generatedBeans, dependencies, CachedBean.class);
		this.cache.update(diagnosticsCacheKey, javaFiles, lastModified, generatedDiagnostics, dependencies, CachedDiagnostics.class);
		
		return scannedTypes;
	}

	private void scanAffectedFiles(IJavaProject project, Set<String> changedTypes, Set<String> alreadyScannedFiles) throws Exception {
		log.info("Start scanning affected files for types {}", changedTypes);
		
		Multimap<String, String> dependencies = dependencyTracker.getAllDependencies();
		Set<String> filesToScan = new HashSet<>();
		
		for (String file : dependencies.keys()) {
			if (!alreadyScannedFiles.contains(file)) {
				Collection<String> dependsOn = dependencies.get(file);
				if (dependsOn.stream().anyMatch(changedTypes::contains)) {
					filesToScan.add(file);
				}
			}
		}
		
		if (!filesToScan.isEmpty()) {
			DocumentDescriptor[] docsToScan = filesToScan.stream().map(file -> {
				File realFile = new File(file);
				String docURI = UriUtil.toUri(realFile).toASCIIString();
				long lastModified = realFile.lastModified();
				return new DocumentDescriptor(docURI, lastModified);
			}).toArray(DocumentDescriptor[]::new);
			
			for (DocumentDescriptor docToScan : docsToScan) {
				this.symbolHandler.removeSymbols(project, docToScan.getDocURI());
			}
			
			scanFilesInternally(project, docsToScan);
		}

		log.info("Finished scanning affected files {}", alreadyScannedFiles);
	}

	private void scanFiles(IJavaProject project, String[] javaFiles, boolean clean) throws Exception {
		IndexCacheKey symbolsCacheKey = getCacheKey(project, SYMBOL_KEY);
		IndexCacheKey beansCacheKey = getCacheKey(project, BEANS_KEY);
		IndexCacheKey diagnosticsCacheKey = getCacheKey(project, DIAGNOSTICS_KEY);

		Pair<CachedSymbol[], Multimap<String, String>> cachedSymbols = this.cache.retrieve(symbolsCacheKey, javaFiles, CachedSymbol.class);
		Pair<CachedBean[], Multimap<String, String>> cachedBeans = this.cache.retrieve(beansCacheKey, javaFiles, CachedBean.class);
		Pair<CachedDiagnostics[], Multimap<String, String>> cachedDiagnostics = this.cache.retrieve(diagnosticsCacheKey, javaFiles, CachedDiagnostics.class);

		CachedSymbol[] symbols;
		CachedBean[] beans;
		CachedDiagnostics[] diagnostics;

		if (clean || cachedSymbols == null || cachedBeans == null || cachedDiagnostics == null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
			List<CachedBean> generatedBeans = new ArrayList<CachedBean>();
			List<CachedDiagnostics> generatedDiagnostics = new ArrayList<CachedDiagnostics>();

			log.info("scan java files, AST parse, pass 1 for files: {}", javaFiles.length);

			BiConsumer<String, Diagnostic> diagnosticsAggregator = new BiConsumer<>() {
				@Override
				public void accept(String docURI, Diagnostic diagnostic) {
					generatedDiagnostics.add(new CachedDiagnostics(docURI, diagnostic));
				}
			};
			
			List<String[]> chunks = createChunks(javaFiles, this.scanChunkSize);
			AnnotationHierarchies annotations = new AnnotationHierarchies();
			for (int i = 0; i < chunks.size(); i++) {

				log.info("scan java files, AST parse, chunk {} for files: {}", i, javaFiles.length);
	            String[] pass2Files = scanFiles(project, annotations, chunks.get(i), generatedSymbols, generatedBeans, diagnosticsAggregator, SCAN_PASS.ONE);

	            if (pass2Files.length > 0) {
					log.info("scan java files, AST parse, pass 2, chunk {} for files: {}", i, javaFiles.length);
					scanFiles(project, annotations, pass2Files, generatedSymbols, generatedBeans, diagnosticsAggregator, SCAN_PASS.TWO);
				}
	        }
			
			log.info("scan java files done, number of symbols created: " + generatedSymbols.size());

			this.cache.store(symbolsCacheKey, javaFiles, generatedSymbols, dependencyTracker.getAllDependencies(), CachedSymbol.class);
			this.cache.store(beansCacheKey, javaFiles, generatedBeans, dependencyTracker.getAllDependencies(), CachedBean.class);
			this.cache.store(diagnosticsCacheKey, javaFiles, generatedDiagnostics, dependencyTracker.getAllDependencies(), CachedDiagnostics.class);
//			dependencyTracker.dump();

			symbols = (CachedSymbol[]) generatedSymbols.toArray(new CachedSymbol[generatedSymbols.size()]);
			beans = (CachedBean[]) generatedBeans.toArray(new CachedBean[generatedBeans.size()]);
			diagnostics = (CachedDiagnostics[]) generatedDiagnostics.toArray(new CachedDiagnostics[generatedDiagnostics.size()]);
		}
		else {
			symbols = cachedSymbols.getLeft();
			beans = cachedBeans.getLeft();
			diagnostics = cachedDiagnostics.getLeft();

			log.info("scan java files used cached data: {} - no. of cached symbols retrieved: {}", project.getElementName(), symbols.length);
			this.dependencyTracker.restore(cachedSymbols.getRight());
			log.info("scan java files restored cached dependency data: {} - no. of cached dependencies: {}", cachedSymbols.getRight().size());
		}

		if (symbols != null && beans != null) {
			EnhancedSymbolInformation[] enhancedSymbols = Arrays.stream(symbols).map(cachedSymbol -> cachedSymbol.getEnhancedSymbol()).toArray(EnhancedSymbolInformation[]::new);
			Bean[] allBeans = Arrays.stream(beans).filter(cachedBean -> cachedBean.getBean() != null).map(cachedBean -> cachedBean.getBean()).toArray(Bean[]::new);
			Map<String, List<Diagnostic>> diagnosticsByDoc = Arrays.stream(diagnostics).filter(cachedDiagnostic -> cachedDiagnostic.getDiagnostic() != null).collect(Collectors.groupingBy(CachedDiagnostics::getDocURI, Collectors.mapping(CachedDiagnostics::getDiagnostic, Collectors.toList())));
			addEmptyDiagnostics(diagnosticsByDoc, javaFiles);
			symbolHandler.addSymbols(project, enhancedSymbols, allBeans, diagnosticsByDoc);
		}
		
		log.info("reconciling stats - counter: " + reconciler.getStatsCounter());
		log.info("reconciling stats - timer: " + reconciler.getStatsTimer());
	}

	private String[] scanFiles(IJavaProject project, AnnotationHierarchies annotations, String[] javaFiles, List<CachedSymbol> generatedSymbols, List<CachedBean> generatedBeans,
			BiConsumer<String, Diagnostic> diagnosticsAggregator, SCAN_PASS pass) throws Exception {
		
		PercentageProgressTask progressTask = this.progressService.createPercentageProgressTask(INDEX_FILES_TASK_ID + project.getElementName(),
				javaFiles.length, "Spring Tools: Indexing Java Sources for '" + project.getElementName() + "'");

		List<String> nextPassFiles = new ArrayList<>();
		final boolean ignoreMethodBodies = SCAN_PASS.ONE.equals(pass);

		FileASTRequestor requestor = new FileASTRequestor() {

			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				File file = new File(sourceFilePath);
				String docURI = UriUtil.toUri(file).toASCIIString();
				long lastModified = file.lastModified();
				AtomicReference<TextDocument> docRef = new AtomicReference<>();

				IProblemCollector problemCollector = problemCollectorCreator.apply(docRef, diagnosticsAggregator);

				SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, sourceFilePath,
						lastModified, docRef, null, generatedSymbols, generatedBeans, problemCollector, pass, nextPassFiles, !ignoreMethodBodies);

				scanAST(context, true);
				progressTask.increment();
			}
		};

		ASTParserCleanupEnabled parser = createParser(project, annotations, ignoreMethodBodies);
		try {
			parser.createASTs(javaFiles, null, new String[0], requestor, null);
			return (String[]) nextPassFiles.toArray(new String[nextPassFiles.size()]);
		} catch (Throwable t) {
			log.error("Failed to index project '%s'".formatted(project.getElementName()), t);
			return new String[0];
		}
		finally {
			parser.cleanup();
			progressTask.done();
		}
	}

	private void addEmptyDiagnostics(Map<String, List<Diagnostic>> diagnosticsByDoc, String[] javaFiles) {
		for (int i = 0; i < javaFiles.length; i++) {
			File file = new File(javaFiles[i]);
			String docURI = UriUtil.toUri(file).toASCIIString();

			if (!diagnosticsByDoc.containsKey(docURI)) {
				diagnosticsByDoc.put(docURI, Collections.emptyList());
			}
		}
	}

	private void scanAST(final SpringIndexerJavaContext context, boolean includeReconcile) {
		context.getCu().accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				try {
					context.addScannedType(node.resolveBinding());
					extractSymbolInformation(node, context);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
				}
				
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				try {
					extractSymbolInformation(node, context);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(node, context);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(node, context);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(node, context);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}
		});
		
		if (includeReconcile) {
			reconcile(context);
		}
		
		dependencyTracker.update(context.getFile(), context.getDependencies());;
	}
	
	private void reconcile(SpringIndexerJavaContext context) {
		// reconciling
		IProblemCollector problemCollector = new IProblemCollector() {
			
			List<ReconcileProblem> problems = new ArrayList<>();
			
			@Override
			public void endCollecting() {
				for (ReconcileProblem p : problems) {
					context.getProblemCollector().accept(p);
				}
			}
			
			@Override
			public void beginCollecting() {
				problems.clear();
			}
			
			@Override
			public void accept(ReconcileProblem problem) {
				problems.add(problem);
			}
		};

		try {
			problemCollector.beginCollecting();
			reconciler.reconcile(context.getProject(), URI.create(context.getDocURI()), context.getCu(), problemCollector, context.isFullAst());
			problemCollector.endCollecting();
		} catch (RequiredCompleteAstException e) {
			if (context.getPass() == SCAN_PASS.TWO) {
				problemCollector.endCollecting();
				log.error("Complete AST required but it is complete already. Parsing ", context.getDocURI());
			} else {
				// Let problems be found in the next pass, don't add the problems to the aggregate problems collector to not duplicate them with the next pass
				context.getNextPassFiles().add(context.getFile());
			}
		}
	}

	private void extractSymbolInformation(TypeDeclaration typeDeclaration, final SpringIndexerJavaContext context) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = DocumentUtils.getTempTextDocument(context.getDocURI(), context.getDocRef(), context.getContent());
			for (SymbolProvider provider : providers) {
				provider.addSymbols(typeDeclaration, context, doc);
			}
		}
	}

	private void extractSymbolInformation(MethodDeclaration methodDeclaration, final SpringIndexerJavaContext context) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = DocumentUtils.getTempTextDocument(context.getDocURI(), context.getDocRef(), context.getContent());
			for (SymbolProvider provider : providers) {
				provider.addSymbols(methodDeclaration, context, doc);
			}
		}
	}

	private void extractSymbolInformation(Annotation node, final SpringIndexerJavaContext context) throws Exception {
		IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();

		if (annotationBinding != null) {
			
			ITypeBinding typeBinding = annotationBinding.getAnnotationType();
			
			// symbol and index scanning
			List<ITypeBinding> metaAnnotations = new ArrayList<>();
			AnnotationHierarchies annotationHierarchies = AnnotationHierarchies.get(node);
			for (Iterator<IAnnotationBinding> itr = annotationHierarchies.iterator(typeBinding); itr.hasNext();) {
				IAnnotationBinding ab = itr.next();
				/*
				 * If meta annotations of the current annotation is a "sub-type" of one of the annotations from symbol providers then add it to meta annotations
				 */
				if (annotationHierarchies.isAnnotatedWithAnnotationByBindingKey(ab, symbolProviders::containsKey)) {
					metaAnnotations.add(ab.getAnnotationType());
				}
			}
			
			Collection<SymbolProvider> providers = symbolProviders.get(annotationHierarchies, annotationBinding);
			if (!providers.isEmpty()) {
				TextDocument doc = DocumentUtils.getTempTextDocument(context.getDocURI(), context.getDocRef(), context.getContent());
				for (SymbolProvider provider : providers) {
					provider.addSymbols(node, typeBinding, metaAnnotations, context, doc);
				}
			} else {
				WorkspaceSymbol symbol = provideDefaultSymbol(node, context);
				if (symbol != null) {
					EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol);
					context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
				}
			}
			
		}
		else {
			log.debug("type binding not around: " + context.getDocURI() + " - " + node.toString());
		}
	}

	private WorkspaceSymbol provideDefaultSymbol(Annotation node, final SpringIndexerJavaContext context) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework") || isJakartaAnnotationWithDefaultSymbol(qualifiedName)) {
					TextDocument doc = DocumentUtils.getTempTextDocument(context.getDocURI(), context.getDocRef(), context.getContent());
					return DefaultSymbolProvider.provideDefaultSymbol(node, doc);
				}
			}
		}
		catch (Exception e) {
			log.error("error creating default symbol in project '" + context.getProject().getElementName() + "' - for docURI '" + context.getDocURI() + "' - on node: " + node.toString(), e);
		}

		return null;
	}

	private boolean isJakartaAnnotationWithDefaultSymbol(String qualifiedName) {
		return jakartaAnnnotations.contains(qualifiedName);
	}

	public static ASTParserCleanupEnabled createParser(IJavaProject project, AnnotationHierarchies annotationHierarchies, boolean ignoreMethodBodies) throws Exception {
		String[] classpathEntries = getClasspathEntries(project);
		String[] sourceEntries = getSourceEntries(project);
		String complianceJavaVersion = getComplianceJavaVersion(project.getClasspath().getJavaVersion());
		
		return new ASTParserCleanupEnabled(classpathEntries, sourceEntries, complianceJavaVersion, annotationHierarchies, ignoreMethodBodies);
	}

	private static String getComplianceJavaVersion(String javaVersion) {
		// Currently the java version in the classpath seems to be 1.8, 17, 21 etc.
		return javaVersion == null || javaVersion.isBlank() ? JavaCore.VERSION_21 : javaVersion;
	}

	private static String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
		return classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath())
				.toArray(String[]::new);
	}

	private static String[] getSourceEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<File> sourceEntries = IClasspathUtil.getSourceFolders(classpath);
		return sourceEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath())
				.toArray(String[]::new);
	}
	
	private Stream<File> foldersToScan(IJavaProject project) {
		IClasspath classpath = project.getClasspath();
		return scanTestJavaSources ? IClasspathUtil.getProjectJavaSourceFolders(classpath)
				: IClasspathUtil.getProjectJavaSourceFoldersWithoutTests(classpath);
	}

	private String[] getFiles(IJavaProject project) throws Exception {
		return foldersToScan(project)
			.flatMap(folder -> {
				try {
					return Files.walk(folder.toPath());
				} catch (IOException e) {
					log.error("{}", e);
					return Stream.empty();
				}
			})
			.filter(path -> isInterestedIn(path.getFileName().toString()))
			.filter(Files::isRegularFile)
			.map(path -> path.toAbsolutePath().toString())
			.toArray(String[]::new);
	}

	private IndexCacheKey getCacheKey(IJavaProject project, String elementType) {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getBinaryRoots(classpath, cpe -> !Classpath.ENTRY_KIND_SOURCE.equals(cpe.getKind())).stream();

		String classpathIdentifier = classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath() + "#" + file.lastModified())
				.collect(Collectors.joining(","));

//		return new IndexCacheKey(project.getElementName(), "java", elementType, DigestUtils.md5Hex(GENERATION + "-" + classpathIdentifier).toUpperCase());
		return new IndexCacheKey(project.getElementName(), "java", elementType, DigestUtils.md5Hex(GENERATION + "-" + validationSeveritySettings.toString() + "-" + classpathIdentifier).toUpperCase());
	}
	
	private List<String[]> createChunks(String[] sourceArray, int chunkSize) {

		int numberOfChunks = (int) Math.ceil((double) sourceArray.length / chunkSize);
		List<String[]> result = new ArrayList<>();

		for (int i = 0; i < numberOfChunks; i++) {
			int startIdx = i * chunkSize;
			int endIdx = Math.min(startIdx + chunkSize, sourceArray.length);

			String[] chunk = new String[endIdx - startIdx];
			System.arraycopy(sourceArray, startIdx, chunk, 0, endIdx - startIdx);
			
			result.add(chunk);
		}
		
		return result;
	}

	public void setScanTestJavaSources(boolean scanTestJavaSources) {
		if (this.scanTestJavaSources != scanTestJavaSources) {
			this.scanTestJavaSources = scanTestJavaSources;
			if (scanTestJavaSources) {
				addTestsJavaSourcesToIndex();
			} else {
				removeTestJavaSourcesFromIndex();
			}
		}
	}
	
	private void removeTestJavaSourcesFromIndex() {
		for (IJavaProject project : projectFinder.all()) {
			Path[] testJavaFiles = IClasspathUtil.getProjectTestJavaSources(project.getClasspath()).flatMap(folder -> {
				try {
					return Files.walk(folder.toPath());
				} catch (IOException e) {
					log.error("{}", e);
					return Stream.empty();
				}
			})
			.filter(path -> path.getFileName().toString().endsWith(".java"))
			.filter(Files::isRegularFile).toArray(Path[]::new);

			try {
				for (Path path : testJavaFiles) {
					URI docUri = UriUtil.toUri(path.toFile());
					symbolHandler.removeSymbols(project, docUri.toASCIIString()); 
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
	
	private void addTestsJavaSourcesToIndex() {
		for (IJavaProject project : projectFinder.all()) {
			Path[] testJavaFiles = IClasspathUtil.getProjectTestJavaSources(project.getClasspath()).flatMap(folder -> {
				try {
					return Files.walk(folder.toPath());
				} catch (IOException e) {
					log.error("{}", e);
					return Stream.empty();
				}
			})
			.filter(path -> path.getFileName().toString().endsWith(".java"))
			.filter(Files::isRegularFile).toArray(Path[]::new);
			
			try {
				for (Path path : testJavaFiles) {
					File file = path.toFile();
					URI docUri = UriUtil.toUri(file);
					String content = FileUtils.readFileToString(file, Charset.defaultCharset());
					scanFile(project, new DocumentDescriptor(docUri.toASCIIString(), file.lastModified()), content);
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	public void setValidationSeveritySettings(JsonObject javaValidationSettingsJson) {
		this.validationSeveritySettings = javaValidationSettingsJson;
	}

	public void setFileScanListener(FileScanListener fileScanListener) {
		this.fileScanListener = fileScanListener;
	}

	public void setScanChunkSize(int chunkSize) {
		this.scanChunkSize = chunkSize;
	}

	private void fileScannedEvent(String file) {
		if (fileScanListener != null) {
			fileScanListener.fileScanned(file);
		}
	}
	
}
