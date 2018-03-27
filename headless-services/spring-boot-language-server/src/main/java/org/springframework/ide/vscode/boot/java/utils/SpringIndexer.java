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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
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
import org.eclipse.lsp4j.WorkspaceFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchyAwareLookup;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
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

	private final Thread updateWorker;
	private final BlockingQueue<WorkerItem> updateQueue;

	private static final Logger log = LoggerFactory.getLogger(SpringIndexer.class);

	private final Listener projectListener = new Listener() {

		@Override
		public void created(IJavaProject project) {
			log.debug("project created event: {}", project.getElementName());
			refresh();
		}

		@Override
		public void changed(IJavaProject project) {
			log.debug("project changed event: {}", project.getElementName());
			refresh();
		}

		@Override
		public void deleted(IJavaProject project) {
			log.debug("project deleted event: {}", project.getElementName());
			refresh();
		}

	};

	private volatile InitializeItem lastInitializeItem;

	public SpringIndexer(SimpleLanguageServer server, BootLanguageServerParams params, AnnotationHierarchyAwareLookup<SymbolProvider> specificProviders) {
		log.debug("Creating {}", this);
		this.server = server;
		this.params = params;
		this.projectFinder = params.projectFinder;
		this.symbolProviders = specificProviders;

		this.symbols = Collections.synchronizedList(new ArrayList<>());
		this.symbolsByDoc = new ConcurrentHashMap<>();
		this.addonInformation = Collections.synchronizedList(new ArrayList<>());
		this.addonInformationByDoc = new ConcurrentHashMap<>();

		this.updateQueue = new LinkedBlockingQueue<>();
		this.updateWorker = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						WorkerItem workerItem = updateQueue.take();
						log.debug("dequeued {}", workerItem);
						workerItem.run();
					}
				}
				catch (InterruptedException e) {
					// ignore
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, "Spring Annotation Index Update Worker");
		server.onInitialized(updateWorker::start);
		getWorkspaceService().onDidChangeWorkspaceFolders(evt -> {
			log.debug("workspace roots have changed event arrived - added: " + evt.getEvent().getAdded() + " - removed: " + evt.getEvent().getRemoved());
			refresh();
		});

		if (getProjectObserver() != null) {
			getProjectObserver().addListener(projectListener);
		}
	}

	private ProjectObserver getProjectObserver() {
		return params.projectObserver;
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

	private SimpleWorkspaceService getWorkspaceService() {
		return server.getServer().getWorkspaceService();
	}

	public CompletableFuture<Void> initialize(Collection<WorkspaceFolder> workspaceRoots) {
		synchronized(this) {
			try {
				if (lastInitializeItem != null && !lastInitializeItem.getFuture().isDone()) {
					log.debug("Canceling {}", lastInitializeItem);
					lastInitializeItem.getFuture().cancel(false);
				}

				lastInitializeItem = new InitializeItem(workspaceRoots.toArray(new WorkspaceFolder[workspaceRoots.size()]));
				updateQueue.put(lastInitializeItem);
				return lastInitializeItem.getFuture();
			}
			catch (Throwable  e) {
				log.error("{}", e);
			}
		}

		return null;
	}

	public boolean isInitializing() {
		return lastInitializeItem != null && !lastInitializeItem.getFuture().isDone();
	}

	public void waitForInitializeTask() {
		synchronized (this) {
			if (lastInitializeItem != null) {
				try {
					log.debug("Wating for {}", lastInitializeItem);
					lastInitializeItem.getFuture().get();
				} catch (InterruptedException | ExecutionException e) {
					// ignore
				}
			}
		}
	}

	private void refresh() {
		synchronized (this) {
			symbols.clear();
			symbolsByDoc.clear();

			addonInformation.clear();
			addonInformationByDoc.clear();

			Collection<WorkspaceFolder> roots = server.getWorkspaceRoots();
			log.debug("refresh spring indexer for roots: {}", roots.toString());
			initialize(roots);
		}
	}

	public void shutdown() {
		try {
			synchronized(this) {
				if (updateWorker != null && updateWorker.isAlive()) {
					updateWorker.interrupt();
				}

				if (getProjectObserver() != null) {
					getProjectObserver().removeListener(projectListener);
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
	}

	public CompletableFuture<Void> updateDocument(String docURI, String content) {
		synchronized(this) {
			if (docURI.endsWith(".java") && lastInitializeItem != null) {
				try {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						String[] classpathEntries = getClasspathEntries(maybeProject.get());

						UpdateItem updateItem = new UpdateItem(docURI, content, classpathEntries);
						updateQueue.put(updateItem);
						return updateItem.getFuture();
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
				DeleteItem deleteItem = new DeleteItem(deletedDocURI);
				updateQueue.put(deleteItem);
				return deleteItem.getFuture();
			}
			catch (Exception e) {
				log.error("{}", e);
			}
		}

		return null;
	}

	public CompletableFuture<Void> createDocument(String docURI) {
		synchronized(this) {
			if (docURI.endsWith(".java") && lastInitializeItem != null) {
				try {
					Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
					if (maybeProject.isPresent()) {
						String[] classpathEntries = getClasspathEntries(maybeProject.get());

						String content = FileUtils.readFileToString(new File(new URI(docURI)));
						UpdateItem updateItem = new UpdateItem(docURI, content, classpathEntries);
						updateQueue.put(updateItem);
						return updateItem.getFuture();
					}
				}
				catch (Exception e) {
					log.error("{}", e);
				}
			}
		}

		return null;
	}

	public List<SymbolInformation> getAllSymbols(String query) {
		waitForInitializeTask();

		if (query != null && query.length() > 0) {
			return searchMatchingSymbols(this.symbols, query);
		} else {
			return this.symbols;
		}
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		waitForInitializeTask();
		return this.symbolsByDoc.get(docURI);
	}

	public List<SymbolAddOnInformation> getAllAdditionalInformation(Predicate<SymbolAddOnInformation> filter) {
		waitForInitializeTask();

		if (filter != null) {
			return addonInformation.stream().filter(filter).collect(Collectors.toList());
		}
		else {
			return null;
		}
	}

	public List<? extends SymbolAddOnInformation> getAdditonalInformation(String docURI) {
		waitForInitializeTask();
		List<SymbolAddOnInformation> info = this.addonInformationByDoc.get(docURI);
		return info == null ? ImmutableList.of() : info;
	}

	private List<SymbolInformation> searchMatchingSymbols(List<SymbolInformation> allsymbols, String query) {
		waitForInitializeTask();
		return allsymbols.stream()
				.filter(symbol -> StringUtil.containsCharactersCaseInsensitive(symbol.getName(), query))
				.collect(Collectors.toList());
	}

	private void scanFiles(WorkspaceFolder directory) {
		try {
			Map<Optional<IJavaProject>, List<String>> projects = Files.walk(Paths.get(new URI(directory.getUri())))
					.filter(path -> path.getFileName().toString().endsWith(".java"))
					.filter(Files::isRegularFile)
					.map(path -> path.toAbsolutePath().toString())
					.collect(Collectors.groupingBy((javaFile) -> projectFinder.find(new TextDocumentIdentifier(new File(javaFile).toURI().toString()))));

			projects.forEach((maybeProject, files) -> maybeProject.ifPresent(project -> scanProject(project, files.toArray(new String[0]))));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanProject(IJavaProject project, String[] files) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS10);
			String[] classpathEntries = getClasspathEntries(project);

			scanFiles(parser, files, classpathEntries);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanFile(String docURI, String content, String[] classpathEntries) throws Exception {
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
			List<SymbolInformation> oldSymbols = symbolsByDoc.remove(docURI);
			if (oldSymbols != null) {
				symbols.removeAll(oldSymbols);
			}

			List<SymbolAddOnInformation> oldAddOnInformation = addonInformationByDoc.remove(docURI);
			if (oldAddOnInformation != null) {
				addonInformation.removeAll(oldAddOnInformation);
			}

			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			scanAST(cu, docURI, docRef, content);
		}
	}

	private void scanFiles(ASTParser parser, String[] javaFiles, String[] classpathEntries) throws Exception {

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
				scanAST(cu, docURI, docRef, null);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);
	}

	private void scanAST(final CompilationUnit cu, final String docURI, AtomicReference<TextDocument> docRef, final String content) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration node) {
				try {
					extractSymbolInformation(node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				try {
					extractSymbolInformation(node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef, content);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}
		});
	}

	private void extractSymbolInformation(TypeDeclaration typeDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(typeDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						symbols.add(enhancedSymbol.getSymbol());
						symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());
						
						if (enhancedSymbol.getAdditionalInformation() != null) {
							addonInformation.addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
							addonInformationByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
						}
					});
				}
			}
		}
	}

	private void extractSymbolInformation(MethodDeclaration methodDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(methodDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						symbols.add(enhancedSymbol.getSymbol());
						symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());

						if (enhancedSymbol.getAdditionalInformation() != null) {
							addonInformation.addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
							addonInformationByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
						}
					});
				}
			}
		}
	}

	private void extractSymbolInformation(Annotation node, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
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
							symbols.add(enhancedSymbol.getSymbol());
							symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(enhancedSymbol.getSymbol());

							if (enhancedSymbol.getAdditionalInformation() != null) {
								addonInformation.addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
								addonInformationByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolAddOnInformation>()).addAll(Arrays.asList(enhancedSymbol.getAdditionalInformation()));
							}
						});
					}
				}
			} else {
				SymbolInformation symbol = provideDefaultSymbol(node, docURI, docRef, content);
				if (symbol != null) {
					symbols.add(symbol);
					symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(symbol);
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
		Stream<Path> classpathEntries = classpath.getClasspathEntries().stream();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

	/**
	 * inner class to capture items for the update worker
	 */
	private interface WorkerItem {

		public void run();
		public CompletableFuture<Void> getFuture();

	}

	private class InitializeItem implements WorkerItem {
		
		private final WorkspaceFolder[] workspaceRoots;
		private final CompletableFuture<Void> future;

		public InitializeItem(WorkspaceFolder[] workspaceRoots) {
			this.workspaceRoots = workspaceRoots;
			this.future = new CompletableFuture<Void>();
			log.debug("{} created ", this);
		}

		@Override
		public CompletableFuture<Void> getFuture() {
			return future;
		}

		@Override
		public void run() {
			log.debug("{} starting...", this);
			try {
				if (!future.isCancelled()) {
//					log.debug("initialze spring indexer task started for roots:   " + Arrays.toString(workspaceRoots));
	
					for (WorkspaceFolder root : workspaceRoots) {
						SpringIndexer.this.scanFiles(root);
					}
	
//					log.debug("initialze spring indexer task completed for roots: " + Arrays.toString(workspaceRoots));
	
					future.complete(null);
					log.debug("{} completed", this);
				}
				else {
					log.debug("{} skipped because it was canceled", this);
				}
			} catch (Throwable e) {
				log.error("{} threw exception", this, e);
			}
		}
	}

	private class UpdateItem implements WorkerItem {

		private final String docURI;
		private final String content;
		private final String[] classpathEntries;

		private final CompletableFuture<Void> future;

		public UpdateItem(String docURI, String content, String[] classpathEntries) {
			this.docURI = docURI;
			this.content = content;
			this.classpathEntries = classpathEntries;
			this.future = new CompletableFuture<Void>();
		}

		@Override
		public CompletableFuture<Void> getFuture() {
			return future;
		}

		@Override
		public void run() {
			try {
				SpringIndexer.this.scanFile(docURI, content, classpathEntries);
			} catch (Exception e) {
				log.error("{}", e);
			}
			future.complete(null);
		}
	}

	private class DeleteItem implements WorkerItem {

		private final String docURI;
		private final CompletableFuture<Void> future;

		public DeleteItem(String docURI) {
			this.docURI = docURI;
			this.future = new CompletableFuture<Void>();
		}

		@Override
		public CompletableFuture<Void> getFuture() {
			return future;
		}

		@Override
		public void run() {
			try {

				List<SymbolInformation> oldSymbols = symbolsByDoc.remove(docURI);
				if (oldSymbols != null) {
					symbols.removeAll(oldSymbols);
				}
				
				List<SymbolAddOnInformation> oldAddInInformation = addonInformationByDoc.remove(docURI);
				if (oldAddInInformation != null) {
					addonInformation.removeAll(oldAddInInformation);
				}

			} catch (Exception e) {
				log.error("{}", e);
			}
			future.complete(null);
		}
	}

}
