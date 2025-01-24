/*******************************************************************************
 * Copyright (c) 2018, 2025 Pivotal, Inc.
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
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouteHighlightProdivder implements HighlightProvider {

	private static final Logger log = LoggerFactory.getLogger(WebfluxRouteHighlightProdivder.class);

	private final SpringMetamodelIndex springIndex;

	public WebfluxRouteHighlightProdivder(SpringMetamodelIndex springIndex) {
		this.springIndex = springIndex;
	}

	@Override
	public void provideHighlights(CancelChecker cancelToken, TextDocument document, Position position, List<DocumentHighlight> resultAccumulator) {
		log.info("PROVIDE HIGHLIGHTS: {} / {}", position.getLine(), position.getCharacter());
		
		cancelToken.checkCanceled();

		Bean[] beans = springIndex.getBeans();
		Arrays.stream(beans)
			.flatMap(bean -> bean.getChildren().stream())
			.filter(element -> element instanceof WebfluxRouteElementRangesIndexElement)
			.map(element -> (WebfluxRouteElementRangesIndexElement) element)
			.filter(rangesElement -> rangesElement.contains(position))
			.flatMap(rangesElement -> Arrays.stream(rangesElement.getRanges()))
			.forEach(range -> resultAccumulator.add(new DocumentHighlight(range)));
	}

}
