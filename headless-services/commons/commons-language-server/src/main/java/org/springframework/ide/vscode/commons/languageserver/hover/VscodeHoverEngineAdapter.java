/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.hover;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

import reactor.util.function.Tuple2;

public class VscodeHoverEngineAdapter implements HoverHandler {

	public enum HoverType {
		MARKDOWN,
		HTML
	}

	private HoverInfoProvider hoverInfoProvider;
	private SimpleLanguageServer server;
	private HoverType type;
	final static Logger log = LoggerFactory.getLogger(VscodeHoverEngineAdapter.class);


	public VscodeHoverEngineAdapter(SimpleLanguageServer server, HoverInfoProvider hoverInfoProvider) {
		this(server, hoverInfoProvider, HoverType.MARKDOWN);
	}

	public VscodeHoverEngineAdapter(SimpleLanguageServer server, HoverInfoProvider hoverInfoProvider, HoverType type) {
		this.hoverInfoProvider = hoverInfoProvider;
		this.server = server;
		this.type = type;
	}

	public void setHoverType(HoverType type) {
		//TODO: is this even used? Check and remove if not.
		this.type = type;
	}

	@Override
	public Hover handle(HoverParams params) {
		try {
			SimpleTextDocumentService documents = server.getTextDocumentService();
			TextDocument doc = documents.get(params.getTextDocument().getUri());
			if (doc != null) {
				int offset = doc.toOffset(params.getPosition());

				Tuple2<Renderable, IRegion> hoverTuple = hoverInfoProvider.getHoverInfo(doc, offset);
				if (hoverTuple != null) {
					Renderable hoverInfo = hoverTuple.getT1();
					IRegion region = hoverTuple.getT2();
					Range range = doc.toRange(region.getOffset(), region.getLength());

					String rendered = render(hoverInfo, type);
					if (StringUtil.hasText(rendered)) {
						Hover hover = new Hover(ImmutableList.of(Either.forLeft(rendered)), range);
						return hover;
					} else {
						log.debug("No hover because rendered hover has no text");
					}
				} else {
					log.debug("No hover because hoverInfoProvider returned no hover");
				}
			} else {
				log.debug("No hover because doc is null");
			}
		} catch (Exception e) {
			log.error("error computing hover", e);
		}
		return SimpleTextDocumentService.NO_HOVER;
	}

	private static String render(Renderable renderable, HoverType type) {
		switch (type) {
		case HTML:
			return renderable.toHtml();
		case MARKDOWN:
			return renderable.toMarkdown();
		default:
			return renderable.toMarkdown();
		}
	}

}
