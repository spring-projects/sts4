/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.hover;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.util.Futures;

public class VscodeHoverEngineAdapter implements VscodeHoverEngine {

	private IHoverEngine engine;
	private SimpleLanguageServer server;
	final static Logger logger = LoggerFactory.getLogger(VscodeHoverEngineAdapter.class);


	public VscodeHoverEngineAdapter(SimpleLanguageServer server, IHoverEngine engine) {
		this.engine = engine;
		this.server = server;
	}

	@Override
	public CompletableFuture<Hover> getHover(TextDocumentPositionParams params) {
		//TODO: This returns a CompletableFuture which suggests we should try to do expensive work asyncly.
		// We are currently just doing all this in a blocking way and wrapping the already computed list into
		// a trivial pre-resolved future.
		try {
			SimpleTextDocumentService documents = server.getTextDocumentService();
			TextDocument doc = documents.get(params);
			if (doc!=null) {
				int offset = doc.toOffset(params.getPosition());
				HoverInfo hoverInfo = engine.getHover(doc, offset);
				if (hoverInfo != null) {
					Hover hover = new Hover();
					hover.setContents(Collections.singletonList(hoverInfo.renderAsMarkdown()));
					return Futures.of(hover);
				}
				else{
					return Futures.of(null);
				}

			}
		} catch (Exception e) {
			logger.error("error computing hover", e);
		}
		return SimpleTextDocumentService.NO_HOVER;
	}

}
