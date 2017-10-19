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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaCompletionEngine implements ICompletionEngine {

	private JavaProjectFinder projectFinder;
	private Map<String, CompletionProvider> completionProviders;
	private JavaSnippetManager snippets;

	public BootJavaCompletionEngine(JavaProjectFinder projectFinder, Map<String, CompletionProvider> specificProviders, JavaSnippetManager snippets) {
		this.projectFinder = projectFinder;
		this.completionProviders = specificProviders;
		this.snippets = snippets;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception {
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
			Collection<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
			completions.addAll(collectCompletionsForAnnotations(node, offset, document));
			completions.addAll(snippets.getCompletions(document, offset, node, cu));
			return completions;
		}

		return Collections.emptyList();
	}

	private Collection<ICompletionProposal> collectCompletionsForAnnotations(ASTNode node, int offset, IDocument doc) {
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
				if (qualifiedName != null) {
					CompletionProvider provider = this.completionProviders.get(qualifiedName);
					if (provider != null) {
						return provider.provideCompletions(exactNode, annotation, type, offset, doc);
					}
				}
			}
		}

		return Collections.emptyList();
	}

	private String[] getClasspathEntries(IDocument doc) throws Exception {
		IJavaProject project = this.projectFinder.find(new TextDocumentIdentifier(doc.getUri())).get();
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries
				.filter(path -> path.toFile().exists())
				.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new);
	}

}
