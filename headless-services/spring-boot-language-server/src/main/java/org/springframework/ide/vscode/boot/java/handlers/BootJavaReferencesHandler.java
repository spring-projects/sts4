/*******************************************************************************
 * Copyright (c) 2017, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CancellationException;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.ReferenceParams;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.ReferencesHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReferencesHandler implements ReferencesHandler {

	private final JavaProjectFinder projectFinder;
	private final BootJavaLanguageServerComponents server;
	private final Map<String, ReferenceProvider> annotationSpecificReferenceProviders;
	private final List<ReferenceProvider> unspecificProviders;
	private final CompilationUnitCache cuCache;

	public BootJavaReferencesHandler(BootJavaLanguageServerComponents server, CompilationUnitCache cuCache, JavaProjectFinder projectFinder,
			Map<String, ReferenceProvider> specificProviders, List<ReferenceProvider> unspecificProviders) {
		this.server = server;
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
		this.annotationSpecificReferenceProviders = specificProviders;
		this.unspecificProviders = unspecificProviders;
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

	private List<? extends Location> provideReferences(CancelChecker cancelToken, TextDocument doc, int offset) throws Exception {
		Optional<IJavaProject> projectOptional = projectFinder.find(doc.getId());

		if (projectOptional.isPresent()) {
			IJavaProject project = projectOptional.get();

			URI docUri = URI.create(doc.getUri());

			return cuCache.withCompilationUnit(project, docUri, cu -> {
				cancelToken.checkCanceled();
				
				ASTNode node = NodeFinder.perform(cu, offset, 0);
				if (node != null) {
					List<Location> result = new ArrayList<>();

					List<? extends Location> referencesForAnnotation = provideReferencesForAnnotation(cancelToken, project, node, offset);
					if (referencesForAnnotation != null) {
						result.addAll(referencesForAnnotation);
					}
					
					List<? extends Location> otherReferences = provideUnspecificReferences(cancelToken, project, doc, node, offset);
					if (otherReferences != null) {
						result.addAll(otherReferences);
					}
					
					return result.size() > 0 ? result : null;
				}
				else {
					return null;
				}
			});
		}

		return null;
	}

	private List<? extends Location> provideUnspecificReferences(CancelChecker cancelToken, IJavaProject project, TextDocument doc, ASTNode node, int offset) {
		List<Location> result = new ArrayList<>();
		
		for (Iterator<ReferenceProvider> iterator = unspecificProviders.iterator(); iterator.hasNext();) {
			ReferenceProvider referenceProvider = iterator.next();
			
			List<? extends Location> references = referenceProvider.provideReferences(cancelToken, project, doc, node, offset);
			if (references != null) {
				result.addAll(references);
			}
		}
		
		return result;
	}

	private List<? extends Location> provideReferencesForAnnotation(CancelChecker cancelToken, IJavaProject project, ASTNode node, int offset) {
		Annotation annotation = null;

		ASTNode annotationNode = node;
		while (annotationNode != null && !(annotationNode instanceof Annotation)) {
			annotationNode = annotationNode.getParent();
		}

		if (annotationNode != null) {
			annotation = (Annotation) annotationNode;
			ITypeBinding type = annotation.resolveTypeBinding();

			if (type != null) {

				String qualifiedName = type.getQualifiedName();
				
				if (qualifiedName != null) {
					ReferenceProvider provider = this.annotationSpecificReferenceProviders.get(qualifiedName);
					
					if (provider != null) {
						return provider.provideReferences(cancelToken, project, node, annotation, type, offset);
					}
				}
			}
		}

		return null;
	}

}
