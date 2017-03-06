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
package org.springframework.ide.vscode.boot.java.references;

import java.nio.file.Path;
import java.util.List;
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
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.springframework.ide.vscode.boot.java.hover.ValueHoverProvider;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReferencesHandler implements ReferencesHandler {

	private static final String SPRING_VALUE = "org.springframework.beans.factory.annotation.Value";

	private JavaProjectFinder projectFinder;
	private SimpleLanguageServer server;

	public BootJavaReferencesHandler(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	@Override
	public CompletableFuture<List<? extends Location>> handle(ReferenceParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.get(params).copy();
		if (doc != null) {
			try {
				int offset = doc.toOffset(params.getPosition());
				CompletableFuture<List<? extends Location>> referencesResult = provideReferences(doc, offset);
				if (referencesResult != null) {
					return referencesResult;
				}
			}
			catch (Exception e) {
			}
		}
		
		return SimpleTextDocumentService.NO_REFERENCES;
	}

	private CompletableFuture<List<? extends Location>> provideReferences(TextDocument document, int offset) throws Exception {
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
			return provideReferencesForAnnotation(node, offset, document);
		}

		return null;
	}

	private CompletableFuture<List<? extends Location>> provideReferencesForAnnotation(ASTNode node, int offset, TextDocument doc) {
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
					return provideReferencesForSpringAnnotation(exactNode, annotation, type, offset, doc);
				}
			}
		}
		
		return null;
	}

	private CompletableFuture<List<? extends Location>> provideReferencesForSpringAnnotation(ASTNode node, Annotation annotation, ITypeBinding type, int offset, TextDocument doc) {
		if (type.getQualifiedName().equals(SPRING_VALUE)) {
			return new ValuePropertyReferencesProvider(server).provideReferencesForValueAnnotation(node, annotation, type, offset, doc);
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
