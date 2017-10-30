/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringIndexer {

	private BootJavaLanguageServer server;
	private JavaProjectFinder projectFinder;
	private Map<String, SymbolProvider> symbolProviders;

	private List<SymbolInformation> symbols;
	private ConcurrentMap<String, List<SymbolInformation>> symbolsByDoc;

	private CompletableFuture<Void> initializeTask;

	private Thread updateWorker;
	private BlockingQueue<UpdateItem> updateQueue;

	private AtomicBoolean initializing = new AtomicBoolean(false);

	private final Listener projectListener = new Listener() {

		@Override
		public void created(IJavaProject project) {
			refresh();
		}

		@Override
		public void changed(IJavaProject project) {
			refresh();
		}

		@Override
		public void deleted(IJavaProject project) {
			refresh();
		}

	};

	public SpringIndexer(BootJavaLanguageServer server, JavaProjectFinder projectFinder, Map<String, SymbolProvider> specificProviders) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.symbolProviders = specificProviders;

		this.symbols = Collections.synchronizedList(new ArrayList<>());
		this.symbolsByDoc = new ConcurrentHashMap<>();

	}

	public CompletableFuture<Void> initialize(final Path workspaceRoot) {
		if (workspaceRoot==null) {
			return CompletableFuture.completedFuture(null);
		}
		synchronized(this) {
			if (this.initializeTask == null) {
				initializing.set(true);
				if (server.getProjectObserver() != null) {
					server.getProjectObserver().addListener(projectListener);
				}
				this.initializeTask = CompletableFuture.runAsync(new Runnable() {
					@Override
					public void run() {
						scanFiles(workspaceRoot.toFile());

						SpringIndexer.this.updateQueue = new LinkedBlockingQueue<>();
						SpringIndexer.this.updateWorker = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									while (true) {
										UpdateItem updateItem = updateQueue.take();
										scanFile(updateItem.getDocURI(), updateItem.getContent(), updateItem.getClasspathEntries());
										updateItem.getFuture().complete(null);
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

						updateWorker.start();
						initializing.set(false);
					}
				});
			}

			return this.initializeTask;
		}
	}

	public boolean isInitializing() {
		return initializing.get();
	}

	private void refresh() {
		synchronized (this) {
			shutdown();
			initializeTask = null;
			symbols.clear();
			symbolsByDoc.clear();
			Log.info("Rebuilding SpringIndexer...");
			initialize(server.getWorkspaceRoot());
		}
	}

	public void shutdown() {
		try {
			if (this.initializeTask != null) {
				initializeTask.cancel(true);
			}

			if (updateWorker != null && updateWorker.isAlive()) {
				updateWorker.interrupt();
			}

			if (server.getProjectObserver() != null) {
				server.getProjectObserver().removeListener(projectListener);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public CompletableFuture<Void> updateDocument(String docURI, String content) {
		if (docURI.endsWith(".java") && initializeTask != null) {
			try {
				initializeTask.get();

				Optional<IJavaProject> maybeProject = projectFinder.find(new TextDocumentIdentifier(docURI));
				if (maybeProject.isPresent()) {
					String[] classpathEntries = getClasspathEntries(maybeProject.get());

					CompletableFuture<Void> future = new CompletableFuture<>();
					UpdateItem updateItem = new UpdateItem(docURI, content, classpathEntries, future);
					updateQueue.put(updateItem);
					return future;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return null;
	}

	public List<SymbolInformation> getAllSymbols(String query) {
		if (initializeTask != null) {
			try {
				initializeTask.get();

				if (query != null && query.length() > 0) {
					return searchMatchingSymbols(this.symbols, query);
				} else {
					return this.symbols;
				}
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		if (initializeTask != null) {
			try {
				initializeTask.get();
				return this.symbolsByDoc.get(docURI);
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private List<SymbolInformation> searchMatchingSymbols(List<SymbolInformation> allsymbols, String query) {
		if (initializeTask != null) {
			try {
				initializeTask.get();
				return allsymbols.stream()
						.filter(symbol -> containsCharacters(symbol.getName().toCharArray(), query.toCharArray()))
						.collect(Collectors.toList());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private boolean containsCharacters(char[] symbolChars, char[] queryChars) {
		int symbolindex = 0;
		int queryindex = 0;

		while (queryindex < queryChars.length && symbolindex < symbolChars.length) {
			if (symbolChars[symbolindex] == queryChars[queryindex]) {
				queryindex++;
			}
			symbolindex++;
		}

		return queryindex == queryChars.length;
	}

	private void scanFiles(File directory) {
		try {
			Map<Optional<IJavaProject>, List<String>> projects = Files.walk(directory.toPath())
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
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			String[] classpathEntries = getClasspathEntries(project);

			scanFiles(parser, files, classpathEntries);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanFile(String docURI, String content, String[] classpathEntries) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

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

			AtomicReference<TextDocument> docRef = new AtomicReference<>();
			scanAST(cu, docURI, docRef, content);
		}
	}

	private void scanFiles(ASTParser parser, String[] javaFiles, String[] classpathEntries) throws Exception {

		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(true);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				String docURI = "file://" + sourceFilePath;
				AtomicReference<TextDocument> docRef = new AtomicReference<>();
				scanAST(cu, docURI, docRef, null);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);
	}

	private void scanAST(final CompilationUnit cu, final String docURI, AtomicReference<TextDocument> docRef, final String content) {
		cu.accept(new ASTVisitor() {

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

	private void extractSymbolInformation(Annotation node, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			String qualifiedTypeName = typeBinding.getQualifiedName();

			SymbolProvider provider = symbolProviders.get(qualifiedTypeName);
			if (provider != null) {
				TextDocument doc = getTempTextDocument(docURI, docRef, content);
				SymbolInformation symbol = provider.getSymbol(node, doc);
				if (symbol != null) {
					symbols.add(symbol);
					symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(symbol);
				}
			}
			else {
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
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

	/**
	 * inner class to capture items for the update worker
	 */
	private static class UpdateItem {

		private final String docURI;
		private final  String content;
		private final String[] classpathEntries;

		private final CompletableFuture<Void> future;

		public UpdateItem(String docURI, String content, String[] classpathEntries, CompletableFuture<Void> future) {
			this.docURI = docURI;
			this.content = content;
			this.classpathEntries = classpathEntries;
			this.future = future;
		}

		public String getDocURI() {
			return docURI;
		}

		public String getContent() {
			return content;
		}

		public String[] getClasspathEntries() {
			return classpathEntries;
		}

		public CompletableFuture<Void> getFuture() {
			return future;
		}

	}
}
