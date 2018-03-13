/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentHighlightHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaDocumentHighlightEngine implements DocumentHighlightHandler {

	private BootJavaLanguageServerComponents server;
	private Collection<HighlightProvider> highlightProviders;

	public BootJavaDocumentHighlightEngine(BootJavaLanguageServerComponents server, Collection<HighlightProvider> highlightProviders) {
		this.server = server;
		this.highlightProviders = highlightProviders;
	}

	@Override
	public CompletableFuture<List<? extends DocumentHighlight>> handle(TextDocumentPositionParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docURI = params.getTextDocument().getUri();

		if (documents.get(docURI) != null) {
			TextDocument doc = documents.get(docURI).copy();
			try {
				CompletableFuture<List<? extends DocumentHighlight>> highlightResult = provideDocumentHighlights(doc);
				if (highlightResult != null) {
					return highlightResult;
				}
			}
			catch (Exception e) {
			}
		}

		return SimpleTextDocumentService.NO_HIGHLIGHTS;
	}

	private CompletableFuture<List<? extends DocumentHighlight>> provideDocumentHighlights(TextDocument document) {
		return server.getCompilationUnitCache().withCompilationUnit(document, cu -> {
			
			if (cu != null) {
				List<DocumentHighlight> result = new ArrayList<>();
				for (HighlightProvider highlightProvider : highlightProviders) {
					highlightProvider.provideHighlights(document, cu, result);
				}
				
				if (result.size() > 0) {
					return CompletableFuture.completedFuture(result);
				}
			}

			return null;
		});
	}

}
