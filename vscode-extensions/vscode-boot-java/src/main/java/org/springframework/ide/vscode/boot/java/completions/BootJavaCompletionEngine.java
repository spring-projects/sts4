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
package org.springframework.ide.vscode.boot.java.completions;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	
	private static final String SPRING_SCOPE = "org.springframework.context.annotation.Scope";
	
	private JavaProjectFinder projectFinder;

	public BootJavaCompletionEngine(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception {
		List<ICompletionProposal> completions = new ArrayList<>();

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
			collectCompletionsForAnnotations(node, completions, offset, document);
		}

		System.out.println("AST node found: " + node.getClass().getName());

		return completions;
	}

	private void collectCompletionsForAnnotations(ASTNode node, List<ICompletionProposal> completions, int offset, IDocument doc) {
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
					collectCompletionsForSpringAnnotation(exactNode, annotation, type, completions, offset, doc);
				}
			}
		}
	}

	private void collectCompletionsForSpringAnnotation(ASTNode node, Annotation annotation, ITypeBinding type,
			List<ICompletionProposal> completions, int offset, IDocument doc) {
		
		if (type.getQualifiedName().equals(SPRING_SCOPE)) {
			new ScopeCompletionProcessor().collectCompletionsForScopeAnnotation(node, annotation, type, completions, offset, doc);
		}
	}

	private String[] getClasspathEntries(IDocument doc) throws Exception {
		IJavaProject project = this.projectFinder.find(doc);
		IClasspath classpath = project.getClasspath();
		Stream<Path> classpathEntries = classpath.getClasspathEntries();
		return classpathEntries.map(path -> path.toAbsolutePath().toString()).toArray(String[]::new); 
	}

}
