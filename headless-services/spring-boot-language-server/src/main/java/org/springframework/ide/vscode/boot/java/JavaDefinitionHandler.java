/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class JavaDefinitionHandler implements DefinitionHandler, LanguageSpecific {
	
	private static final Logger log = LoggerFactory.getLogger(JavaDefinitionHandler.class);

	private CompilationUnitCache cuCache;
	private JavaProjectFinder projectFinder;
	private Collection<IJavaDefinitionProvider> providers;
	
	public JavaDefinitionHandler(CompilationUnitCache cuCache, JavaProjectFinder projectFinder,
			Collection<IJavaDefinitionProvider> providers) {
		this.cuCache = cuCache;
		this.projectFinder = projectFinder;
		this.providers = providers;
	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return Collections.singleton(LanguageId.JAVA);
	}

	@Override
	public List<LocationLink> handle(CancelChecker cancelToken, DefinitionParams definitionParams) {
		TextDocumentIdentifier doc = definitionParams.getTextDocument();
		IJavaProject project = projectFinder.find(doc).orElse(null);
		if (project != null) {
			URI docUri = URI.create(doc.getUri());
			return cuCache.withCompilationUnit(project, docUri, cu -> {
				Builder<LocationLink> builder = ImmutableList.builder();
				if (cu != null) {
					int start = cu.getPosition(definitionParams.getPosition().getLine() + 1, definitionParams.getPosition().getCharacter());
					ASTNode node = NodeFinder.perform(cu, start, 0);
					for (IJavaDefinitionProvider provider : providers) {
						if (cancelToken.isCanceled()) {
							break;
						}
						try {
							builder.addAll(provider.getDefinitions(cancelToken, project, doc, cu, node, start));
						} catch (Exception e) {
							log.error("", e);
						}
					}
				}
				return builder.build();
			});
		}
		return Collections.emptyList();
	}

}
