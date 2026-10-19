/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentHighlight;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensHandler;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.InlayHintHandler;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;
import org.springframework.ide.vscode.commons.util.text.Region;

import com.google.gson.JsonParser;

/**
 * Guards for the handlers that are only wired up in the language server components.
 *
 * @author Martin Lippert
 */
public class FeatureFlagGuardsTest {

	private BootJavaConfig config = new BootJavaConfig(null);

	private void turnOff(FeatureFlag feature) {
		config.handleConfigurationChange(new Settings(JsonParser.parseString(
				"{\"boot-java\": {\"feature-flags\": {\"" + feature.getId() + "\": false}}}")));
	}

	@Test
	void highlightProvider() {
		HighlightProvider delegate = (cancelToken, doc, position, result) -> result.add(new DocumentHighlight(new Range(position, position)));
		HighlightProvider guarded = FeatureFlagGuards.onlyWhen(config, FeatureFlag.WEBFLUX_ROUTE_HIGHLIGHT, delegate);

		List<DocumentHighlight> result = new ArrayList<>();
		guarded.provideHighlights(null, null, new Position(1, 1), result);
		assertEquals(1, result.size());

		turnOff(FeatureFlag.WEBFLUX_ROUTE_HIGHLIGHT);
		result.clear();
		guarded.provideHighlights(null, null, new Position(1, 1), result);
		assertTrue(result.isEmpty());
	}

	@Test
	void inlayHintHandler() {
		InlayHintHandler delegate = (doc, range, cancelChecker) -> List.of(new InlayHint());
		InlayHintHandler guarded = FeatureFlagGuards.onlyWhen(config, FeatureFlag.POM_INLAY_HINTS, delegate);

		assertEquals(1, guarded.handle(null, null, null).size());

		turnOff(FeatureFlag.POM_INLAY_HINTS);
		assertTrue(guarded.handle(null, null, null).isEmpty());
	}

	@Test
	void semanticTokensHandler() {
		SemanticTokensHandler guarded = FeatureFlagGuards.onlyWhen(config, FeatureFlag.CRON_SEMANTIC_TOKENS, new SemanticTokensHandler() {
			@Override
			public org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions getCapability() {
				return new org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions();
			}

			@Override
			public List<SemanticTokenData> semanticTokensFull(org.springframework.ide.vscode.commons.util.text.TextDocument doc, org.eclipse.lsp4j.jsonrpc.CancelChecker cancelChecker) {
				return List.of(new SemanticTokenData(new Region(0, 1), "number", new String[0]));
			}
		});

		assertEquals(1, guarded.semanticTokensFull(null, null).size());

		turnOff(FeatureFlag.CRON_SEMANTIC_TOKENS);
		assertNull(guarded.semanticTokensFull(null, null));
		assertTrue(guarded.getCapability() != null);
	}

	@Test
	void documentSymbolHandler() {
		DocumentSymbolHandler delegate = params -> List.of(new DocumentSymbol());
		DocumentSymbolHandler guarded = FeatureFlagGuards.onlyWhen(config, FeatureFlag.DOCUMENT_SYMBOLS, delegate);

		assertEquals(1, guarded.handle(null).size());

		turnOff(FeatureFlag.DOCUMENT_SYMBOLS);
		assertTrue(guarded.handle(null).isEmpty());
	}

	@Test
	void workspaceSymbolHandler() {
		WorkspaceSymbolHandler delegate = params -> List.of(new WorkspaceSymbol());
		WorkspaceSymbolHandler guarded = FeatureFlagGuards.onlyWhen(config, FeatureFlag.WORKSPACE_SYMBOLS, delegate);

		assertEquals(1, guarded.handle(null).size());

		turnOff(FeatureFlag.WORKSPACE_SYMBOLS);
		assertTrue(guarded.handle(null).isEmpty());
	}

}
