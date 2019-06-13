/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.lsp4j.SymbolInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.base.Supplier;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * @author Martin Lippert
 */
public class SpringIndexerJava implements SpringIndexer {
	
	public static enum SCAN_PASS {
		ONE, TWO
	}

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	private final SymbolHandler symbolHandler;
	private final AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;
	private final SymbolCache cache;
	private final JavaProjectFinder projectFinder;
	private boolean scanTestJavaSources = false;

	private final DependencyTracker dependencyTracker = new DependencyTracker();

	static class DependencyTracker {
		
		private Multimap<String, String> dependencies = MultimapBuilder.hashKeys().hashSetValues().build();
		
		public void addDependency(String sourceFile, ITypeBinding dependsOn) {
			dependencies.put(sourceFile, dependsOn.getKey());
		}
		
		public void dump() {
			log.info("=== Dependencies ===");
			for (String sourceFile : dependencies.keySet()) {
				Collection<String> values = dependencies.get(sourceFile);
				if (!values.isEmpty())
				log.info(sourceFile + "=> ");
				for (String v : values) {
					log.info("   "+v);
				}
			}
			log.info("======================");
		}

		public Multimap<String, String> getAllDependencies() {
			return dependencies;
		}

		public void update(String file, Set<String> dependenciesForFile) {
			dependencies.replaceValues(file, dependenciesForFile);
		}

		public void restore(Multimap<String, String> deps) {
			this.dependencies = deps;
		}
	}
	
	public SpringIndexerJava(SymbolHandler symbolHandler, AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders, SymbolCache cache,
			JavaProjectFinder projectFimder) {
		this.symbolHandler = symbolHandler;
		this.symbolProviders = symbolProviders;
		this.cache = cache;
		this.projectFinder = projectFimder;
	}

	@Override
	public String[] getFileWatchPatterns() {
		return new String[] {"**/*.java"};
	}

	@Override
	public boolean isInterestedIn(String docURI) {
		return docURI.endsWith(".java");
	}

	@Override
	public void initializeProject(IJavaProject project) throws Exception {
		String[] files = this.getFiles(project);

		log.info("scan java files for symbols for project: {} - no. of files: {}", project.getElementName(), files.length);

		long startTime = System.currentTimeMillis();
		scanFiles(project, files);
		long endTime = System.currentTimeMillis();

		log.info("scan java files for symbols for project: {} took ms: {}", project.getElementName(), endTime - startTime);
	}

	@Override
	public void removeProject(IJavaProject project) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		this.cache.remove(cacheKey);
	}

	@Override
	public void updateFile(IJavaProject project, String docURI, long lastModified, Supplier<String> content) throws Exception {
		if (shouldProcessDocument(project, docURI)) {
			scanFile(project, docURI, lastModified, content.get());
		}
	}

	@Override
	public void removeFile(IJavaProject project, String docURI) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		String file = new File(new URI(docURI)).getAbsolutePath();
		this.cache.removeFile(cacheKey, file);
	}
	
	private boolean shouldProcessDocument(IJavaProject project, String docURI) {
		Path path = Paths.get(URI.create(docURI));
		return foldersToScan(project)
				.filter(sourceFolder -> path.startsWith(sourceFolder.toPath()))
				.findFirst()
				.isPresent();
	}

	private void scanFile(IJavaProject project, String docURI, long lastModified, String content) throws Exception {
		//TODO: optimise? Check last modified to avoid redundant scan. Reason:
		//  on saving a file, this may be triggered twice. Once when file is saved and once more because of a 'file changed'
		//  on file system. Looking at the timestamp in the cache we should be able to avoid a second scan of the exact same
		//  content.
		ASTParser parser = createParser(project, false);

		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		log.debug("Scan file: {}", unitName);
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu != null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			String file = UriUtil.toFileString(docURI);

			Set<String> changedTypes = new HashSet<>();
			SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, file,
					lastModified, docRef, content, generatedSymbols, SCAN_PASS.ONE, new ArrayList<>(), changedTypes);

			scanAST(context);

			SymbolCacheKey cacheKey = getCacheKey(project);
			this.cache.update(cacheKey, file, lastModified, generatedSymbols, context.getDependencies());
//			dependencyTracker.dump();

			for (CachedSymbol symbol : generatedSymbols) {
				symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
			}
			Set<String> scannedFiles = new HashSet<>();
			scannedFiles.add(file);
			scanAffectedFiles(project, changedTypes, scannedFiles);
		}
	}

	private void scanAffectedFiles(IJavaProject project, Set<String> changedTypes, Set<String> scannedFiles) {
		log.info("Start scanning affected files for types {}", changedTypes);
		//TODO: optimise? When multiple files are 'affected', we could try to parse and scan them in batch. 
		// I.e. something similar to the 'scanFiles' method.
		// That is probably more efficient than one by one.
		
		Multimap<String, String> dependencies = dependencyTracker.getAllDependencies();
//		Collection<String> filesToScan = new HashSet<>();
		boolean scannedAnyFiles;
		do {
			scannedAnyFiles = false;
			for (String file : dependencies.keys()) {
				try {
					if (!scannedFiles.contains(file)) {
						Collection<String> dependsOn = dependencies.get(file);
						if (dependsOn.stream().anyMatch(changedTypes::contains)) {
							scannedFiles.add(file);
							scannedAnyFiles = true;
							log.debug("Should also scan affected file: {}", file);
							File f = new File(file);
							scanAffectedFile(project, UriUtil.toUri(f).toString(), f.lastModified(), FileUtils.readFileToString(f), changedTypes);
						}
					}
				} catch (Exception e) {
					log.debug("Problems scanning file {}", file, e);
				}
			}
			if (scannedAnyFiles) {
				log.debug("Some affected files where scanned, make another pass");
			}
		} while (scannedAnyFiles);
		log.info("Finished scanning affected files {}", scannedFiles);
	}

	private void scanAffectedFile(IJavaProject project, String docURI, long lastModified, String content, Set<String> changedTypes) throws Exception {
		symbolHandler.removeSymbols(project, docURI);
		ASTParser parser = createParser(project, false);
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu != null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			File file = UriUtil.toFile(docURI);
			if (file!=null) {
				SpringIndexerJavaContext context = new SpringIndexerJavaContext(
						project, cu, 
						docURI, file.toString(), lastModified, docRef, 
						content, generatedSymbols, 
						SCAN_PASS.ONE, new ArrayList<>(), changedTypes);
				scanAST(context);

				SymbolCacheKey cacheKey = getCacheKey(project);
				this.cache.update(cacheKey, file.getAbsolutePath(), lastModified, generatedSymbols, context.getDependencies());
//				dependencyTracker.dump();

				for (CachedSymbol symbol : generatedSymbols) {
					symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
				}
			}
		}
	}

	private void scanFiles(IJavaProject project, String[] javaFiles) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		Pair<CachedSymbol[], Multimap<String, String>> cached = this.cache.retrieve(cacheKey, javaFiles);

		CachedSymbol[] symbols;
		if (cached == null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();

			log.info("scan java files, AST parse, pass 1 for files: {}", javaFiles.length);

			String[] pass2Files = scanFiles(project, javaFiles, generatedSymbols, SCAN_PASS.ONE);
			if (pass2Files.length > 0) {

				log.info("scan java files, AST parse, pass 2 for files: {}", javaFiles.length);

				scanFiles(project, pass2Files, generatedSymbols, SCAN_PASS.TWO);
			}

			this.cache.store(cacheKey, javaFiles, generatedSymbols, dependencyTracker.getAllDependencies());
//			dependencyTracker.dump();

			symbols = (CachedSymbol[]) generatedSymbols.toArray(new CachedSymbol[generatedSymbols.size()]);
		}
		else {
			symbols = cached.getLeft();
			log.info("scan java files used cached data: {} - no. of cached symbols retrieved: {}", project.getElementName(), symbols.length);
			this.dependencyTracker.restore(cached.getRight());
			log.info("scan java files restored cached dependency data: {} - no. of cached dependencies: {}", cached.getRight().size());
		}

		if (symbols != null) {
			for (int i = 0; i < symbols.length; i++) {
				CachedSymbol symbol = symbols[i];
				symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
			}
		}
	}

	private String[] scanFiles(IJavaProject project, String[] javaFiles, List<CachedSymbol> generatedSymbols, SCAN_PASS pass)
			throws Exception {
		ASTParser parser = createParser(project, SCAN_PASS.ONE.equals(pass));
		List<String> nextPassFiles = new ArrayList<>();

		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				File file = new File(sourceFilePath);
				String docURI = UriUtil.toUri(file).toString();
				long lastModified = file.lastModified();
				AtomicReference<TextDocument> docRef = new AtomicReference<>();

				SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, sourceFilePath,
						lastModified, docRef, null, generatedSymbols, pass, nextPassFiles, null);

				scanAST(context);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);

		return (String[]) nextPassFiles.toArray(new String[nextPassFiles.size()]);
	}

	private void scanAST(final SpringIndexerJavaContext context) {
		context.getCu().accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				try {
					Set<String> changedTypes = context.getChangedTypes();
					if (changedTypes!=null) {
						ITypeBinding changedType = node.resolveBinding();
						if (changedType!=null) {
							changedTypes.add(changedType.getKey());
						}
					}
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
		dependencyTracker.update(context.getFile(), context.getDependencies());;
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
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			Collection<SymbolProvider> providers = symbolProviders.get(typeBinding);
			Collection<ITypeBinding> metaAnnotations = AnnotationHierarchies.getMetaAnnotations(typeBinding, symbolProviders::containsKey);
			if (!providers.isEmpty()) {
				TextDocument doc = DocumentUtils.getTempTextDocument(context.getDocURI(), context.getDocRef(), context.getContent());
				for (SymbolProvider provider : providers) {
					provider.addSymbols(node, typeBinding, metaAnnotations, context, doc);
				}
			} else {
				SymbolInformation symbol = provideDefaultSymbol(node, context);
				if (symbol != null) {
					EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, null);
					context.getGeneratedSymbols().add(new CachedSymbol(context.getDocURI(), context.getLastModified(), enhancedSymbol));
				}
			}
		}
	}

	private SymbolInformation provideDefaultSymbol(Annotation node, final SpringIndexerJavaContext context) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
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

	private ASTParser createParser(IJavaProject project, boolean ignoreMethodBodies) throws Exception {
		String[] classpathEntries = getClasspathEntries(project);
		String[] sourceEntries = getSourceEntries(project);

		ASTParser parser = ASTParser.newParser(AST.JLS11);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(ignoreMethodBodies);

		parser.setEnvironment(classpathEntries, sourceEntries, null, false);
		return parser;
	}

	private String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
		return classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath())
				.toArray(String[]::new);
	}

	private String[] getSourceEntries(IJavaProject project) throws Exception {
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
			.filter(path -> path.getFileName().toString().endsWith(".java"))
			.filter(Files::isRegularFile)
			.map(path -> path.toAbsolutePath().toString())
			.toArray(String[]::new);
	}

	private SymbolCacheKey getCacheKey(IJavaProject project) {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();

		String classpathIdentifier = classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath() + "#" + file.lastModified())
				.collect(Collectors.joining(","));

		return new SymbolCacheKey(project.getElementName() + "-java-", DigestUtils.md5Hex(classpathIdentifier).toUpperCase());
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
					symbolHandler.removeSymbols(project, docUri.toString()); 
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
					String content = FileUtils.readFileToString(file);
					scanFile(project, docUri.toString(), file.lastModified(), content);
				}
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

}
