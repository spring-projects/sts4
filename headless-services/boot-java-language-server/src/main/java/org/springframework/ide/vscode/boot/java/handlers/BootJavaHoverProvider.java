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
package org.springframework.ide.vscode.boot.java.handlers;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingHoverProvider;
import org.springframework.ide.vscode.boot.java.value.ValueHoverProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaHoverProvider implements HoverHandler {

	private JavaProjectFinder projectFinder;
	private SimpleLanguageServer server;

	private Map<String, HoverProvider> hoverProviders;

	public BootJavaHoverProvider(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.hoverProviders = new HashMap<>();

		RequestMappingHoverProvider.register(this.hoverProviders);
		ValueHoverProvider.register(this.hoverProviders);
	}

	@Override
	public CompletableFuture<Hover> handle(TextDocumentPositionParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		if (documents.get(params) != null) {
			TextDocument doc = documents.get(params).copy();
			try {
				int offset = doc.toOffset(params.getPosition());
				CompletableFuture<Hover> hoverResult = provideHover(doc, offset);
				if (hoverResult != null) {
					return hoverResult;
				}
			}
			catch (Exception e) {
			}
		}

		return SimpleTextDocumentService.NO_HOVER;
	}

	private CompletableFuture<Hover> provideHover(TextDocument document, int offset) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setResolveBindings(true);

		String[] classpathEntries = getClasspathEntries(document);
		String[] sourceEntries = new String[] {};
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);

		String docURI = document.getUri();
		String unitName = docURI.substring(docURI.lastIndexOf("/"));
		parser.setUnitName(unitName);
		parser.setSource(document.get(0, document.getLength()).toCharArray());

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		ASTNode node = NodeFinder.perform(cu, offset, 0);

		if (node != null) {
			System.out.println("AST node found: " + node.getClass().getName());
			return provideHoverForAnnotation(node, offset, document);
		}

		return null;
	}

	private CompletableFuture<Hover> provideHoverForAnnotation(ASTNode node, int offset, TextDocument doc) {
		Annotation annotation = null;
		ASTNode exactNode = node;

		while (node != null && !(node instanceof Annotation)) {
			node = node.getParent();
		}

		if (node != null) {
			annotation = (Annotation) node;
			ITypeBinding type = annotation.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null && qualifiedName.startsWith("org.springframework")) {
					return provideHoverForSpringAnnotation(exactNode, annotation, type, offset, doc);
				}
			}
		}

		return null;
	}

	private CompletableFuture<Hover> provideHoverForSpringAnnotation(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc) {
		String typeName = type.getQualifiedName();
		HoverProvider provider = this.hoverProviders.get(typeName);
		if (provider != null) {
			return provider.provideHover(node, annotation, type, offset, doc);
		}

		return null;
	}

	private String[] getClasspathEntries(IDocument doc) throws Exception {
		IJavaProject project = this.projectFinder.find(doc);
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

}
