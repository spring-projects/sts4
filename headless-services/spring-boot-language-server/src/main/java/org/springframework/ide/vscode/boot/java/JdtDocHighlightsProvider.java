/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtDocHighlightsProvider implements HighlightProvider {
	
	private static final Logger log = LoggerFactory.getLogger(JdtDocHighlightsProvider.class);
	
	private final JavaProjectFinder projectFinder;
	private final CompilationUnitCache cuCache;
	private final Collection<JdtAstDocHighlightsProvider> astHighlightProviders;

	
	public JdtDocHighlightsProvider(JavaProjectFinder projectFinder, CompilationUnitCache cuCache, Collection<JdtAstDocHighlightsProvider> astHighlightProviders) {
		this.projectFinder = projectFinder;
		this.cuCache = cuCache;
		this.astHighlightProviders = astHighlightProviders;
	}

	@Override
	public void provideHighlights(CancelChecker cancelToken, TextDocument doc, Position p,
			List<DocumentHighlight> resultAccumulator) {
		if (!astHighlightProviders.isEmpty()) {
			IJavaProject project = projectFinder.find(doc.getId()).orElse(null);
			if (project != null) {
				URI docUri = URI.create(doc.getUri());
				cuCache.withCompilationUnit(project, docUri, cu -> {
					if (cu != null) {
						int start = cu.getPosition(p.getLine() + 1, p.getCharacter());
						ASTNode node = NodeFinder.perform(cu, start, 0);
						for (JdtAstDocHighlightsProvider provider : astHighlightProviders) {
							try {
								resultAccumulator.addAll(provider.getDocHighlights(project, doc, cu, node, start));
							} catch (Throwable t) {
								log.error("", t);
							}
						}
					}
					return null;
				});
			}
		}
	}

}
