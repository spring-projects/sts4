/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouteHighlightProdivder implements HighlightProvider {

	private static final Logger log = LoggerFactory.getLogger(WebfluxRouteHighlightProdivder.class);

	private final SpringSymbolIndex springIndexer;

	public WebfluxRouteHighlightProdivder(SpringSymbolIndex indexer) {
		this.springIndexer = indexer;
	}

	@Override
	public void provideHighlights(TextDocument document, Position position, List<DocumentHighlight> resultAccumulator) {
		log.info("PROVIDE HIGHLIGHTS: {} / {}", position.getLine(), position.getCharacter());

		this.springIndexer.getAdditonalInformation(document.getUri())
			.stream()
			.filter(addon -> {
				if (addon instanceof WebfluxElementsInformation) {
					WebfluxElementsInformation handlerInfo = (WebfluxElementsInformation) addon;

					if (handlerInfo.contains(position)) {
						return true;
					}
				}
				return false;
			})
			.flatMap(addon -> Arrays.asList(((WebfluxElementsInformation) addon).getRanges()).stream())
			.forEach(range -> resultAccumulator.add(new DocumentHighlight(range)));
	}

}
