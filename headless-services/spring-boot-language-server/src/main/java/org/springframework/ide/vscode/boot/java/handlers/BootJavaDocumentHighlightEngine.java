/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentHighlightParams;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentHighlightHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaDocumentHighlightEngine implements DocumentHighlightHandler {

	private static Logger log = LoggerFactory.getLogger(BootJavaDocumentHighlightEngine.class);

	private BootJavaLanguageServerComponents server;
	private Collection<HighlightProvider> highlightProviders;

	public BootJavaDocumentHighlightEngine(BootJavaLanguageServerComponents server, Collection<HighlightProvider> highlightProviders) {
		this.server = server;
		this.highlightProviders = highlightProviders;
	}

	@Override
	public List<DocumentHighlight> handle(DocumentHighlightParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		String docURI = params.getTextDocument().getUri();

		if (documents.get(docURI) != null) {
			TextDocument doc = documents.get(docURI).copy();
			// Spring Boot LS get events from boot properties files as well, so filter them out
			if (doc != null && server.getInterestingLanguages().contains(doc.getLanguageId())) {
				try {
					return provideDocumentHighlights(doc, params.getPosition());
				}
				catch (Exception e) {
					log.error("", e);
				}
			}
		}

		return SimpleTextDocumentService.NO_HIGHLIGHTS;
	}

	private List<DocumentHighlight> provideDocumentHighlights(TextDocument document, Position position) {
		List<DocumentHighlight> result = new ArrayList<>();
		for (HighlightProvider highlightProvider : highlightProviders) {
			highlightProvider.provideHighlights(document, position, result);
		}

		return result;
	}

}
