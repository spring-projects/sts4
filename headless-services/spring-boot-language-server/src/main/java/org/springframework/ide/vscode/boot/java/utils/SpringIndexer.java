/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.Futures;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class SpringIndexer {

	private final SimpleLanguageServer server;
	private final BootLanguageServerParams params;
	private final JavaProjectFinder projectFinder;
	private final AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;

	private final List<SymbolInformation> symbols;
	private final List<SymbolAddOnInformation> addonInformation;

	private final ConcurrentMap<String, List<SymbolInformation>> symbolsByDoc;
	private final ConcurrentMap<String, List<SymbolAddOnInformation>> addonInformationByDoc;

	private final ConcurrentMap<String, List<SymbolInformation>> symbolsByProject;
	private final ConcurrentMap<String, List<SymbolAddOnInformation>> addonInformationByProject;

	private final ExecutorService updateQueue;

	private static final Logger log = LoggerFactory.getLogger(SpringIndexer.class);

	private final Listener projectListener = new Listener() {
		@Override
		public void created(IJavaProject project) {
			log.debug("project created event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void changed(IJavaProject project) {
			log.debug("project changed event: {}", project.getElementName());
			initializeProject(project);
		}

		@Override
		public void deleted(IJavaProject project) {
			log.debug("project deleted event: {}", project.getElementName());
			deleteProject(project);
		}
	};

	private SimpleWorkspaceService getWorkspaceService() {
		return server.getServer().getWorkspaceService();
	}

	private ProjectObserver getProjectObserver() {
		return params.projectObserver;
	}

	public SpringIndexer(SimpleLanguageServer server, BootLanguageServerParams params, AnnotationHierarchyAwareLookup<SymbolProvider> specificProviders) {
		log.debug("Creating {}", this);
		this.server = server;
		this.params = params;
		this.projectFinder = params.projectFinder;
		this.symbolProviders = specificProviders;

		this.symbols = Collections.synchronizedList(new ArrayList<>());
		this.symbolsByDoc = new ConcurrentHashMap<>();
		this.symbolsByProject = new ConcurrentHashMap<>();
		this.addonInformation = Collections.synchronizedList(new ArrayList<>());
		this.addonInformationByDoc = new ConcurrentHashMap<>();
		this.addonInformationByProject = new ConcurrentHashMap<>();

		this.updateQueue = Executors.newSingleThreadExecutor();
		
		getWorkspaceService().onDidChangeWorkspaceFolders(evt -> {
			log.debug("workspace roots have changed event arrived - added: " + evt.getEvent().getAdded() + " - removed: " + evt.getEvent().getRemoved());
		});

		if (getProjectObserver() != null) {
			getProjectObserver().addListener(projectListener);
		}
	}

	public void serverInitialized() {
		List<String> globPattern = Arrays.asList("**/*.java");
		getWorkspaceService().getFileObserver().onFileDeleted(globPattern, (file) -> {
			deleteDocument(new TextDocumentIdentifier(file).getUri());
		});
		getWorkspaceService().getFileObserver().onFileCreated(globPattern, (file) -> {
			createDocument(new TextDocumentIdentifier(file).getUri());
		});
	}

	public void shutdown() {
		try {
			synchronized(this) {
				if (updateQueue != null && !updateQueue.isShutdown()) {
					updateQueue.shutdownNow();
				}

				if (getProjectObserver() != null) {
					getProjectObserver().removeListener(projectListener);
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
	}

	public CompletableFuture<Void> initializeProject(IJavaProject project) {
		try {
			InitializeProject initializeItem = new InitializeProject(project);
			return CompletableFuture.runAsync(initializeItem, this.updateQueue);
		} catch (Throwable  e) {
			log.error("", e);
			return Futures.error(e);
		}
	}

	public CompletableFuture<Void> deleteProject(IJavaProject project) {
		try {
			DeleteProject initializeItem = new DeleteProject(project);
			return CompletableFuture.runAsync(initializeItem, this.updateQueue);
		} catch (Throwable  e) {
			log.error("", e);
			return Futures.error(e);
		}
	}

	public CompletableFuture<Void> updateDocument(String docURI, String content) {
		synchronized(this) {
			if (docURI.endsWith(".java")) {
				try {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						String[] classpathEntries = getClasspathEntries(maybeProject.get());
						
						UpdateItem updateItem = new UpdateItem(maybeProject.get(), docURI, content, classpathEntries);
						return CompletableFuture.runAsync(updateItem, this.updateQueue);
					}
				}
				catch (Exception e) {
					log.error("{}", e);
				}
			}
		}

		return null;
	}

	public CompletableFuture<Void> deleteDocument(String deletedDocURI) {
		synchronized(this) {
			try {
				Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(deletedDocURI));
				if (maybeProject.isPresent()) {
					DeleteItem deleteItem = new DeleteItem(maybeProject.get(), deletedDocURI);
					return CompletableFuture.runAsync(deleteItem, this.updateQueue);
				}
			}
			catch (Exception e) {
				log.error("", e);
				return Futures.error(e);
			}
		}
		
		return null;
	}

	public CompletableFuture<Void> createDocument(String docURI) {
		synchronized(this) {
			if (docURI.endsWith(".java")) {
				try {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						String[] classpathEntries = getClasspathEntries(maybeProject.get());

						String content = FileUtils.readFileToString(new File(new URI(docURI)));
						UpdateItem updateItem = new UpdateItem(maybeProject.get(), docURI, content, classpathEntries);
						return CompletableFuture.runAsync(updateItem, this.updateQueue);
					}
				}
				catch (Exception e) {
					log.error("", e);
					return Futures.error(e);
				}
			}
		}
		return CompletableFuture.completedFuture(null);
	}

	public List<SymbolInformation> getAllSymbols(String query) {
		if (query != null && query.length() > 0) {
			return searchMatchingSymbols(this.symbols, query);
		} else {
			return this.symbols;
		}
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		return this.symbolsByDoc.get(docURI);
	}

	public List<SymbolAddOnInformation> getAllAdditionalInformation(Predicate<SymbolAddOnInformation> filter) {
		if (filter != null) {
			return addonInformation.stream().filter(filter).collect(Collectors.toList());
		}
		else {
			return null;
		}
	}

	public List<? extends SymbolAddOnInformation> getAdditonalInformation(String docURI) {
		List<SymbolAddOnInformation> info = this.addonInformationByDoc.get(docURI);
		return info == null ? ImmutableList.of() : info;
	}
	
	/**
	 * inserts a noop operation into the worker/update quene, which allows invokers to use the
	 * returned future to wait for the queue items in the queue to be completed which got inserted before
	 * this noop.
	 */
	public CompletableFuture<Void> waitOperation() {
		return CompletableFuture.runAsync(new Runnable() {
			@Override
			public void run() {
			}
		}, this.updateQueue);
	}

	private List<SymbolInformation> searchMatchingSymbols(List<SymbolInformation> allsymbols, String query) {
		return allsymbols.stream()
				.filter(symbol -> StringUtil.containsCharactersCaseInsensitive(symbol.getName(), query))
				.collect(Collectors.toList());
	}

	private void scanProject(IJavaProject project, String[] files) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS10);
			String[] classpathEntries = getClasspathEntries(project);

			scanFiles(project, parser, files, classpathEntries);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanFile(IJavaProject project, String docURI, String content, String[] classpathEntries) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_10, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(false);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(content.toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		if (cu != null) {
			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			scanAST(project, cu, docURI, docRef, content);
		}
	}

	private void scanFiles(IJavaProject project, ASTParser parser, String[] javaFiles, String[] classpathEntries) throws Exception {

		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_10, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(false);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				String docURI = UriUtil.toUri(new File(sourceFilePath)).toString();
				AtomicReference<TextDocument> docRef = new AtomicReference<>();
				scanAST(project, cu, docURI, docRef, null);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);
	}

	private void scanAST(final IJavaProject project, final CompilationUnit cu, final String docURI, AtomicReference<TextDocument> docRef, final String content) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}
		});
	}

	private void extractSymbolInformation(IJavaProject project, TypeDeclaration typeDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(typeDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						addSymbol(project, docURI, enhancedSymbol);
					});
				}
			}
		}
	}

	private void extractSymbolInformation(IJavaProject project, MethodDeclaration methodDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(methodDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						addSymbol(project, docURI, enhancedSymbol);
					});
				}
			}
		}
	}

	private void extractSymbolInformation(IJavaProject project, Annotation node, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			Collection<SymbolProvider> providers = symbolProviders.get(typeBinding);
			Collection<ITypeBinding> metaAnnotations = AnnotationHierarchies.getMetaAnnotations(typeBinding, symbolProviders::containsKey);
			if (!providers.isEmpty()) {
				TextDocument doc = getTempTextDocument(docURI, docRef, content);
				for (SymbolProvider provider : providers) {
					Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(node, typeBinding, metaAnnotations, doc);
					if (sbls != null) {
						sbls.forEach(enhancedSymbol -> {
							addSymbol(project, docURI, enhancedSymbol);
						});
					}
				}
			} else {
				SymbolInformation symbol = provideDefaultSymbol(node, docURI, docRef, content);
				if (symbol != null) {
					addSymbol(project, docURI, new EnhancedSymbolInformation(symbol, null));
				}
			}
		}
	}

	private TextDocument getTempTextDocument(String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		TextDocument doc = docRef.get();
		if (doc == null) {
			doc = createTempTextDocument(docURI, content);
			docRef.set(doc);
		}
		return doc;
	}

	private TextDocument createTempTextDocument(String docURI, String content) throws Exception {
		if (content == null) {
			Path path = Paths.get(new URI(docURI));
			content = new String(Files.readAllBytes(path));
		}

		TextDocument doc = new TextDocument(docURI, LanguageId.PLAINTEXT, 0, content);
		return doc;
	}

	private SymbolInformation provideDefaultSymbol(Annotation node, String docURI, AtomicReference<TextDocument> docRef, String content) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
					TextDocument doc = getTempTextDocument(docURI, docRef, content);
					SymbolInformation symbol = new SymbolInformation(node.toString(), SymbolKind.Interface,
							new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
					return symbol;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private String[] getClasspathEntries(IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
		return classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath())
				.toArray(String[]::new);
	}


	private class InitializeProject implements Runnable {
		
		private final IJavaProject project;

		public InitializeProject(IJavaProject project) {
			this.project = project;
			log.debug("{} created ", this);
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				removeSymbolsByProject(project);

				URI projectUri = project.getLocationUri();
				List<String> files = Files.walk(Paths.get(projectUri))
					.filter(path -> path.getFileName().toString().endsWith(".java"))
					.filter(Files::isRegularFile)
					.map(path -> path.toAbsolutePath().toString())
					.collect(Collectors.toList());

				SpringIndexer.this.scanProject(project, (String[]) files.toArray(new String[files.size()]));
	
				log.debug("{} completed", this);
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}
		
	}

	private class DeleteProject implements Runnable {
		
		private final IJavaProject project;

		public DeleteProject(IJavaProject project) {
			this.project = project;
			log.debug("{} created ", this);
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				removeSymbolsByProject(project);
				log.debug("{} completed", this);
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}
		
	}

	private class UpdateItem implements Runnable {

		private final String docURI;
		private final String content;
		private final String[] classpathEntries;
		private final IJavaProject project;

		public UpdateItem(IJavaProject project, String docURI, String content, String[] classpathEntries) {
			this.project = project;
			this.docURI = docURI;
			this.content = content;
			this.classpathEntries = classpathEntries;
		}

		@Override
		public void run() {
			try {
				removeSymbolsByDoc(project, docURI);
				SpringIndexer.this.scanFile(project, docURI, content, classpathEntries);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}

	private class DeleteItem implements Runnable {

		private final String docURI;
		private IJavaProject project;

		public DeleteItem(IJavaProject project, String docURI) {
			this.project = project;
			this.docURI = docURI;
		}

		@Override
		public void run() {
			try {
				removeSymbolsByDoc(project, docURI);
			} catch (Exception e) {
				log.error("{}", e);
			}
		}
	}
	
	private void addSymbol(IJavaProject project, String docURI, EnhancedSymbolInformation enhancedSymbol) {
		symbols.add(enhancedSymbol.getSymbol());
		symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());
		symbolsByProject.computeIfAbsent(project.getElementName(), s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());
		
		if (enhancedSymbol.getAdditionalInformation() != null) {
			addonInformation.addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
			addonInformationByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
			addonInformationByProject.computeIfAbsent(project.getElementName(), s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
		}
	}
	
	private void removeSymbolsByDoc(IJavaProject project, String docURI) {
		List<SymbolInformation> oldSymbols = symbolsByDoc.remove(docURI);
		if (oldSymbols != null) {
			symbols.removeAll(oldSymbols);

			List<SymbolInformation> projectSymbols = symbolsByProject.get(project.getElementName());
			if (projectSymbols != null) {
				projectSymbols.removeAll(oldSymbols);
			}
		}
		
		List<SymbolAddOnInformation> oldAddInInformation = addonInformationByDoc.remove(docURI);
		if (oldAddInInformation != null) {
			addonInformation.removeAll(oldAddInInformation);

			List<SymbolAddOnInformation> projectAddOns = addonInformationByProject.get(project.getElementName());
			if (projectAddOns != null) {
				projectAddOns.removeAll(oldAddInInformation);
			}
		}
		
	}

	private void removeSymbolsByProject(IJavaProject project) {
		List<SymbolInformation> oldSymbols = symbolsByProject.remove(project.getElementName());
		if (oldSymbols != null) {
			symbols.removeAll(oldSymbols);

			Set<String> keySet = symbolsByDoc.keySet();
			Iterator<String> docIter = keySet.iterator();
			while (docIter.hasNext()) {
				String docURI = docIter.next();
				List<SymbolInformation> docSymbols = symbolsByDoc.get(docURI);
				docSymbols.removeAll(oldSymbols);
				
				if (docSymbols.isEmpty()) {
					docIter.remove();
				}
			}
		}
		
		List<SymbolAddOnInformation> oldAddInInformation = addonInformationByProject.remove(project.getElementName());
		if (oldAddInInformation != null) {
			addonInformation.removeAll(oldAddInInformation);

			Set<String> keySet = addonInformationByDoc.keySet();
			Iterator<String> docIter = keySet.iterator();
			while (docIter.hasNext()) {
				String docURI = docIter.next();
				List<SymbolAddOnInformation> docAddons = addonInformationByDoc.get(docURI);
				docAddons.removeAll(oldAddInInformation);
				
				if (docAddons.isEmpty()) {
					docIter.remove();
				}
			}
		}
		
	}

}
