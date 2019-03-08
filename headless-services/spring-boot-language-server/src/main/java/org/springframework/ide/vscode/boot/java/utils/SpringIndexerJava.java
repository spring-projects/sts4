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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

	private static final Logger log = LoggerFactory.getLogger(SpringIndexerJava.class);

	private final SymbolHandler symbolHandler;
	private final AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders;

	public SpringIndexerJava(SymbolHandler symbolHandler, AnnotationHierarchyAwareLookup<SymbolProvider> symbolProviders) {
		this.symbolHandler = symbolHandler;
		this.symbolProviders = symbolProviders;
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
		List<String> files = Files.walk(Paths.get(project.getLocationUri()))
				.filter(path -> path.getFileName().toString().endsWith(".java"))
				.filter(Files::isRegularFile)
				.map(path -> path.toAbsolutePath().toString())
				.collect(Collectors.toList());

		scanProject(project, (String[]) files.toArray(new String[files.size()]));
	}

	@Override
	public void updateFile(IJavaProject project, String docURI, String content) throws Exception {
		String[] classpathEntries = getClasspathEntries(project);
		scanFile(project, docURI, content, classpathEntries);
	}


	private void scanProject(IJavaProject project, String[] files) {
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS11);
			String[] classpathEntries = getClasspathEntries(project);

			scanFiles(project, parser, files, classpathEntries);
		}
		catch (Exception e) {
			log.error("error parsing all Java source files from project: " + project.getElementName(), e);
		}
	}

	private void scanFile(IJavaProject project, String docURI, String content, String[] classpathEntries) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS11);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(false);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, false);

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
		JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);
		parser.setIgnoreMethodBodies(false);

		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, false);

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
					log.error("error extracting symbol information in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MethodDeclaration node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					extractSymbolInformation(project, node, docURI, docRef, content);
				}
				catch (Exception e) {
					log.error("error extracting symbol information in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
				}

				return super.visit(node);
			}
		});
	}

	private void extractSymbolInformation(IJavaProject project, TypeDeclaration typeDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = DocumentUtils.getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(typeDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						symbolHandler.addSymbol(project, docURI, enhancedSymbol);
					});
				}
			}
		}
	}

	private void extractSymbolInformation(IJavaProject project, MethodDeclaration methodDeclaration, String docURI, AtomicReference<TextDocument> docRef, String content) throws Exception {
		Collection<SymbolProvider> providers = symbolProviders.getAll();
		if (!providers.isEmpty()) {
			TextDocument doc = DocumentUtils.getTempTextDocument(docURI, docRef, content);
			for (SymbolProvider provider : providers) {
				Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(methodDeclaration, doc);
				if (sbls != null) {
					sbls.forEach(enhancedSymbol -> {
						symbolHandler.addSymbol(project, docURI, enhancedSymbol);
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
				TextDocument doc = DocumentUtils.getTempTextDocument(docURI, docRef, content);
				for (SymbolProvider provider : providers) {
					Collection<EnhancedSymbolInformation> sbls = provider.getSymbols(node, typeBinding, metaAnnotations, doc);
					if (sbls != null) {
						sbls.forEach(enhancedSymbol -> {
							symbolHandler.addSymbol(project, docURI, enhancedSymbol);
						});
					}
				}
			} else {
				SymbolInformation symbol = provideDefaultSymbol(project, node, docURI, docRef, content);
				if (symbol != null) {
					symbolHandler.addSymbol(project, docURI, new EnhancedSymbolInformation(symbol, null));
				}
			}
		}
	}

	private SymbolInformation provideDefaultSymbol(IJavaProject project, Annotation node, String docURI, AtomicReference<TextDocument> docRef, String content) {
		try {
			ITypeBinding type = node.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
					TextDocument doc = DocumentUtils.getTempTextDocument(docURI, docRef, content);
					return DefaultSymbolProvider.provideDefaultSymbol(node, doc);
				}
			}
		}
		catch (Exception e) {
			log.error("error creating default symbol in project '" + project.getElementName() + "' - for docURI '" + docURI + "' - on node: " + node.toString(), e);
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

}
