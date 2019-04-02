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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.codec.digest.DigestUtils;
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
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

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

	public SpringIndexerJava(SymbolHandler symbolHandler, AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders, SymbolCache cache) {
		this.symbolHandler = symbolHandler;
		this.symbolProviders = symbolProviders;
		this.cache = cache;
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
	public void updateFile(IJavaProject project, String docURI, long lastModified, String content) throws Exception {
		scanFile(project, docURI, lastModified, content);
	}

	@Override
	public void removeFile(IJavaProject project, String docURI) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		String file = new File(new URI(docURI)).getAbsolutePath();
		this.cache.removeFile(cacheKey, file);
	}

	private void scanFile(IJavaProject project, String docURI, long lastModified, String content) throws Exception {
		ASTParser parser = createParser(project, false);

		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu != null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();
			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			String file = new File(new URI(docURI)).getAbsolutePath();

			SpringIndexerJavaContext context = new SpringIndexerJavaContext(project, cu, docURI, file,
					lastModified, docRef, content, generatedSymbols, SCAN_PASS.ONE, new ArrayList<>());

			scanAST(context);

			SymbolCacheKey cacheKey = getCacheKey(project);
			this.cache.update(cacheKey, file, lastModified, generatedSymbols);

			for (CachedSymbol symbol : generatedSymbols) {
				symbolHandler.addSymbol(project, symbol.getDocURI(), symbol.getEnhancedSymbol());
			}
		}
	}

	private void scanFiles(IJavaProject project, String[] javaFiles) throws Exception {
		SymbolCacheKey cacheKey = getCacheKey(project);
		CachedSymbol[] symbols = this.cache.retrieve(cacheKey, javaFiles);

		if (symbols == null) {
			List<CachedSymbol> generatedSymbols = new ArrayList<CachedSymbol>();

			log.info("scan java files, AST parse, pass 1 for files: {}", javaFiles.length);

			String[] pass2Files = scanFiles(project, javaFiles, generatedSymbols, SCAN_PASS.ONE);
			if (pass2Files.length > 0) {

				log.info("scan java files, AST parse, pass 2 for files: {}", javaFiles.length);

				scanFiles(project, pass2Files, generatedSymbols, SCAN_PASS.TWO);
			}

			this.cache.store(cacheKey, javaFiles, generatedSymbols);

			symbols = (CachedSymbol[]) generatedSymbols.toArray(new CachedSymbol[generatedSymbols.size()]);
		}
		else {
			log.info("scan java files used cached data: {} - no. of cached symbols retrieved: {}", project.getElementName(), symbols.length);
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
						lastModified, docRef, null, generatedSymbols, pass, nextPassFiles);

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

	private String[] getFiles(IJavaProject project) throws Exception {
		return Files.walk(Paths.get(project.getLocationUri()))
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

}
