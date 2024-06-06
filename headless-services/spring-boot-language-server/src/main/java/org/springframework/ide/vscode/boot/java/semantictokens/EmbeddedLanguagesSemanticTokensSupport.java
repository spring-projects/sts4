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
package org.springframework.ide.vscode.boot.java.semantictokens;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.DocumentFilter;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

public class EmbeddedLanguagesSemanticTokensSupport {

	private static final String CAPABILITY = "textDocument/semanticTokens";

	private final SimpleLanguageServer server;
	private boolean enabled;

	private String registrationId;
	private boolean initialized;

	public EmbeddedLanguagesSemanticTokensSupport(SimpleLanguageServer server, BootJavaConfig config) {
		this.server = server;
		this.enabled = false;
		this.initialized = false;
		this.registrationId = null;
		config.addListener(v -> setEnabled(config.isJavaEmbeddedLanguagesSyntaxHighlighting()));
	}

	public synchronized boolean isEnabled() {
		return enabled;
	}

	private synchronized void setEnabled(boolean enabled) {
		if (!initialized || this.enabled != enabled) {
			this.initialized = true;
			this.enabled = enabled;
			SemanticTokensWithRegistrationOptions semanticTokensCapability = server.getTextDocumentService()
					.getSemanticTokensWithRegistrationOptions();
			if (semanticTokensCapability != null) {
				List<DocumentFilter> newDocSelectors = semanticTokensCapability.getDocumentSelector().stream()
						.filter(s -> !LanguageId.JAVA.getId().equals(s.getLanguage())).toList();
				if (newDocSelectors.size() != semanticTokensCapability.getDocumentSelector().size()) {
					if (!enabled) {
						semanticTokensCapability.setDocumentSelector(newDocSelectors);
					}
					CompletableFuture<?> unregisterFuture = CompletableFuture.completedFuture(null);
					if (this.registrationId != null) {
						UnregistrationParams unregisterParams = new UnregistrationParams(
								List.of(new Unregistration(this.registrationId, CAPABILITY)));
						unregisterFuture = server.getClient().unregisterCapability(unregisterParams)
								.thenAccept(v -> this.registrationId = null);
					}

					unregisterFuture.thenCompose(res -> {
						final String registrationId = UUID.randomUUID().toString();
						RegistrationParams registerParams = new RegistrationParams(
								List.of(new Registration(registrationId, CAPABILITY, semanticTokensCapability)));
						return server.getClient().registerCapability(registerParams)
								.thenAccept(v -> this.registrationId = registrationId);
					});
				}

			}
		}
	}

}
