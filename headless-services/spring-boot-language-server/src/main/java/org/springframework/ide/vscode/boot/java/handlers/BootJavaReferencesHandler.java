/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.io.File;
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
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReferencesHandler implements ReferencesHandler {

	private JavaProjectFinder projectFinder;
	private BootJavaLanguageServerComponents server;
	private Map<String, ReferenceProvider> referenceProviders;

	public BootJavaReferencesHandler(BootJavaLanguageServerComponents server, JavaProjectFinder projectFinder, Map<String, ReferenceProvider> specificProviders) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.referenceProviders = specificProviders;
	}

	@Override
	public List<? extends Location> handle(ReferenceParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.get(params).copy();
		if (doc != null) {
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (server.getInterestingLanguages().contains(doc.getLanguageId())) {
				try {
					int offset = doc.toOffset(params.getPosition());
					List<? extends Location> referencesResult = provideReferences(doc, offset);
					if (referencesResult != null) {
						return referencesResult;
					}
				}
				catch (Exception e) {
				}
			}
		}

		return SimpleTextDocumentService.NO_REFERENCES;
	}

	private List<? extends Location> provideReferences(TextDocument document, int offset) throws Exception {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_10, options);
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
			return provideReferencesForAnnotation(node, offset, document);
		}

		return null;
	}

	private List<? extends Location> provideReferencesForAnnotation(ASTNode node, int offset, TextDocument doc) {
		Annotation annotation = null;

		while (node != null && !(node instanceof Annotation)) {
			node = node.getParent();
		}

		if (node != null) {
			annotation = (Annotation) node;
			ITypeBinding type = annotation.resolveTypeBinding();
			if (type != null) {
				String qualifiedName = type.getQualifiedName();
				if (qualifiedName != null) {
					ReferenceProvider provider = this.referenceProviders.get(qualifiedName);
					if (provider != null) {
						return provider.provideReferences(node, annotation, type, offset, doc);
					}
				}
			}
		}

		return null;
	}

	private String[] getClasspathEntries(IDocument doc) throws Exception {
		IJavaProject project = this.projectFinder.find(new TextDocumentIdentifier(doc.getUri())).get();
		IClasspath classpath = project.getClasspath();
		Stream<File> classpathEntries = IClasspathUtil.getAllBinaryRoots(classpath).stream();
		return classpathEntries
				.filter(file -> file.exists())
				.map(file -> file.getAbsolutePath())
				.toArray(String[]::new);
	}

}
