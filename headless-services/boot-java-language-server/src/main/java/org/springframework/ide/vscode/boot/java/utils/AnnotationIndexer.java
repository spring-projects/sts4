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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class AnnotationIndexer {

	private JavaProjectFinder projectFinder;
	private Map<String, SymbolProvider> symbolProviders;

	private List<SymbolInformation> symbols;

	public AnnotationIndexer(JavaProjectFinder projectFinder, Map<String, SymbolProvider> specificProviders) {
		this.projectFinder = projectFinder;
		this.symbolProviders = specificProviders;

		this.symbols = new ArrayList<>();
	}

	public void reset() {
		this.symbols.clear();
	}

	public List<? extends SymbolInformation> getAllSymbols() {
		return symbols;
	}

	public void scanFiles(File directory) {
		try {
			Path[] javaFiles = Files.walk(directory.toPath())
					.filter(Files::isRegularFile)
					.filter(path -> path.getFileName().toString().endsWith(".java"))
					.toArray(Path[]::new);

			Map<IJavaProject, List<String>> projects = new HashMap<>();
			for (Path javaFile : javaFiles) {
				IJavaProject project = projectFinder.find(javaFile.toFile());
				if (project != null) {
					if (!projects.containsKey(project)) {
						projects.put(project, new ArrayList<>());
					}

					projects.get(project).add(javaFile.toAbsolutePath().toString());
				}
			}

			projects.forEach((project, files) -> scanProject(project, files.toArray(new String[0])));

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

	private void scanFiles(ASTParser parser, String[] javaFiles, String[] classpathEntries) throws Exception {
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		FileASTRequestor requestor = new FileASTRequestor() {
			@Override
			public void acceptAST(String sourceFilePath, CompilationUnit cu) {
				String docURI = "file://" + sourceFilePath;
				scanAST(cu, docURI);
			}
		};

		parser.createASTs(javaFiles, null, new String[0], requestor, null);
	}

	private void scanAST(final CompilationUnit cu, final String docURI) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(node, docURI);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(node, docURI);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(node, docURI);
				}
				catch (Exception e) {
					e.printStackTrace();
				}

				return super.visit(node);
			}
		});
	}

	private void extractSymbolInformation(Annotation node, String docURI) throws Exception {
		System.out.println("annotation found: " + node.toString());
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			String qualifiedTypeName = typeBinding.getQualifiedName();

			TextDocument doc = createTempTextDocument(docURI);

			SymbolProvider provider = symbolProviders.get(qualifiedTypeName);
			if (provider != null) {
				SymbolInformation symbol = provider.getSymbol(node, doc);
				if (symbol != null) {
					symbols.add(symbol);
				}
			}
			else {
				SymbolInformation symbol = provideDefaultSymbol(node, doc);
				if (symbol != null) {
					symbols.add(symbol);
				}
			}
		}
	}

	private TextDocument createTempTextDocument(String docURI) throws Exception {
		Path path = Paths.get(new URI(docURI));
		String content = new String(Files.readAllBytes(path));

		TextDocument doc = new TextDocument(docURI, LanguageId.PLAINTEXT, 0, content);
		return doc;
	}

	private SymbolInformation provideDefaultSymbol(Annotation node, TextDocument doc) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
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
