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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringIndexer {

	private SimpleLanguageServer server;
	private JavaProjectFinder projectFinder;
	private Map<String, SymbolProvider> symbolProviders;

	private List<SymbolInformation> symbols;
	private ConcurrentMap<String, List<SymbolInformation>> symbolsByDoc;

	private CompletableFuture<Void> initializeTask;

	public SpringIndexer(SimpleLanguageServer server, JavaProjectFinder projectFinder, Map<String, SymbolProvider> specificProviders) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.symbolProviders = specificProviders;

		this.symbols = Collections.synchronizedList(new ArrayList<>());
		this.symbolsByDoc = new ConcurrentHashMap<>();
	}

	public void initialize() {
		synchronized(this) {
			if (this.initializeTask == null) {
				this.initializeTask = CompletableFuture.runAsync(new Runnable() {
					@Override
					public void run() {
						System.out.println("start initial scan...");
						Path workspaceRoot = server.getWorkspaceRoot();
						reset();
						scanFiles(workspaceRoot.toFile());
						System.out.println("initial scan done...!!!");
					}
				});
			}
		}

		try {
			this.initializeTask.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void reset() {
		this.symbols.clear();
		this.symbolsByDoc.clear();
	}

	public void updateDocument(String docURI) {
		// TODO: update information because of doc change
	}

	public List<? extends SymbolInformation> getAllSymbols() {
		initialize();
		return this.symbols;
	}

	public List<? extends SymbolInformation> getSymbols(String docURI) {
		initialize();
		return this.symbolsByDoc.get(docURI);
	}

	public void scanFiles(File directory) {
		try {
			System.out.println("scan directory...");

			Map<IJavaProject, List<String>> projects = Files.walk(directory.toPath())
					.filter(path -> path.getFileName().toString().endsWith(".java"))
					.filter(Files::isRegularFile)
					.map(path -> path.toAbsolutePath().toString())
					.collect(Collectors.groupingBy((javaFile) -> projectFinder.find(new File(javaFile))));

			System.out.println("scan directory done!!!");

			projects.forEach((project, files) -> scanProject(project, files.toArray(new String[0])));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void scanProject(IJavaProject project, String[] files) {
		try {
			System.out.println("create parser... " + project.getElementName());
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			String[] classpathEntries = getClasspathEntries(project);
			System.out.println("create parser done!!!");

			System.out.println("parse files... " + project.getElementName());
			scanFiles(parser, files, classpathEntries);
			System.out.println("parse files done!!!");
		}
		catch (Exception e) {
			e.printStackTrace();
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
				scanAST(cu, docURI, docRef);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);
	}

	private void scanAST(final CompilationUnit cu, final String docURI, AtomicReference<TextDocument> docRef) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(node, docURI, docRef);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}
		});
	}

	private void extractSymbolInformation(Annotation node, String docURI, AtomicReference<TextDocument> docRef) throws Exception {
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			String qualifiedTypeName = typeBinding.getQualifiedName();

			SymbolProvider provider = symbolProviders.get(qualifiedTypeName);
			if (provider != null) {
				TextDocument doc = getTempTextDocument(docURI, docRef);
				SymbolInformation symbol = provider.getSymbol(node, doc);
				if (symbol != null) {
					symbols.add(symbol);
					symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(symbol);
				}
			}
			else {
				SymbolInformation symbol = provideDefaultSymbol(node, docURI, docRef);
				if (symbol != null) {
					symbols.add(symbol);
					symbolsByDoc.computeIfAbsent(docURI, s -> new ArrayList<SymbolInformation>()).add(symbol);
				}
			}
		}
	}

	private TextDocument getTempTextDocument(String docURI, AtomicReference<TextDocument> docRef) throws Exception {
		TextDocument doc = docRef.get();
		if (doc == null) {
			doc = createTempTextDocument(docURI);
			docRef.set(doc);
		}
		return doc;
	}

	private TextDocument createTempTextDocument(String docURI) throws Exception {
		Path path = Paths.get(new URI(docURI));
		String content = new String(Files.readAllBytes(path));

		TextDocument doc = new TextDocument(docURI, LanguageId.PLAINTEXT, 0, content);
		return doc;
	}

	private SymbolInformation provideDefaultSymbol(Annotation node, String docURI, AtomicReference<TextDocument> docRef) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
					TextDocument doc = getTempTextDocument(docURI, docRef);
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

}
