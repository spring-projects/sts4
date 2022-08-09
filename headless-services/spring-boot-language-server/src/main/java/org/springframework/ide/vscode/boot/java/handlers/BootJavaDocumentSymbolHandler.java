/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collections;
import java.util.List;

import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaDocumentSymbolHandler implements DocumentSymbolHandler {

	private SpringSymbolIndex indexer;
	private BootJavaLanguageServerComponents server;

	public BootJavaDocumentSymbolHandler(BootJavaLanguageServerComponents server, SpringSymbolIndex indexer) {
		this.server = server;
		this.indexer = indexer;
	}

	@Override
	public List<? extends WorkspaceSymbol> handle(DocumentSymbolParams params) {
		SimpleTextDocumentService documents = server.getTextDocumentService();
		TextDocument doc = documents.getLatestSnapshot(params.getTextDocument().getUri());

		if (server.getInterestingLanguages().contains(doc.getLanguageId())) {
			return indexer.getSymbols(params.getTextDocument().getUri());			
		}
		
		return Collections.emptyList();
	}

}
