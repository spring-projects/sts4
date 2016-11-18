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
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.IRegion;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.languageserver.util.TextDocument;
import org.springframework.ide.vscode.commons.util.Futures;

import reactor.util.function.Tuple2;

public class VscodeHoverEngineAdapter implements VscodeHoverEngine {

	private HoverInfoProvider hoverInfoProvider;
	private SimpleLanguageServer server;
	final static Logger logger = LoggerFactory.getLogger(VscodeHoverEngineAdapter.class);


	public VscodeHoverEngineAdapter(SimpleLanguageServer server, HoverInfoProvider hoverInfoProvider) {
		this.hoverInfoProvider = hoverInfoProvider;
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

				Tuple2<HoverInfo, IRegion> hoverTuple = hoverInfoProvider.getHoverInfo(doc, offset);
				if (hoverTuple != null) {
					HoverInfo hoverInfo = hoverTuple.getT1();
					IRegion region = hoverTuple.getT2();
					Range range = doc.toRange(region.getOffset(), region.getLength());

					Hover hover = new Hover(Collections.singletonList(hoverInfo.toMarkdown()), range);

					return Futures.of(hover);
				}
			}
		} catch (Exception e) {
			logger.error("error computing hover", e);
		}
		return SimpleTextDocumentService.NO_HOVER;
	}

}
