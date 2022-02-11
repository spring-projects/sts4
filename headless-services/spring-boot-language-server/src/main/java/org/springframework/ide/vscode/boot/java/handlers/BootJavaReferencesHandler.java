/*******************************************************************************
 * Copyright (c) 2017, 2021 Pivotal, Inc.
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
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.stream.Stream;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.ORCompilationUnitCache;
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
	private ORCompilationUnitCache cuCache;

	public BootJavaReferencesHandler(BootJavaLanguageServerComponents server, JavaProjectFinder projectFinder, Map<String, ReferenceProvider> specificProviders, ORCompilationUnitCache cuCache) {
		this.server = server;
		this.projectFinder = projectFinder;
		this.referenceProviders = specificProviders;
		this.cuCache = cuCache;
		
	}

	@Override
	public List<? extends Location> handle(CancelChecker cancelToken, ReferenceParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();		
		TextDocument doc = documents.getLatestSnapshot(params);

		if (doc != null) {
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (server.getInterestingLanguages().contains(doc.getLanguageId())) {
				try {
					int offset = doc.toOffset(params.getPosition());
					
					cancelToken.checkCanceled();
					
					List<? extends Location> referencesResult = provideReferences(cancelToken, doc, offset);
					if (referencesResult != null) {
						return referencesResult;
					}
				}
				catch (CancellationException e) {
					throw e;
				}
				catch (Exception e) {
				}
			}
		}

		return SimpleTextDocumentService.NO_REFERENCES;
	}

	private List<? extends Location> provideReferences(CancelChecker cancelToken, TextDocument document, int offset) throws Exception {
		Optional<IJavaProject> project = projectFinder.find(document.getId());
		if (project.isPresent()) {
			return cuCache.withCompilationUnit(project.get(), URI.create(document.getUri()), cu -> {
				J node = ORAstUtils.findAstNodeAt(cu, offset);
				
				if (node != null) {
					cancelToken.checkCanceled();
					return provideReferencesForAnnotation(cancelToken, node, offset, document);
				}

				return null;
			});
			
		}
		return null;
	}

	private List<? extends Location> provideReferencesForAnnotation(CancelChecker cancelToken, J node, int offset, TextDocument doc) {
		if (node != null) {
			Annotation annotation = ORAstUtils.findNode(node, Annotation.class);

			if (annotation != null) {
				FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
				if (type != null) {
						ReferenceProvider provider = this.referenceProviders.get(type.getFullyQualifiedName());
						if (provider != null) {
							return provider.provideReferences(cancelToken, node, annotation, type, offset, doc);
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
