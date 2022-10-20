/*******************************************************************************
 * Copyright (c) 2019, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

import java.util.Optional;
import java.util.Set;

import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

import com.google.common.collect.ImmutableSet;

/**
 * @author Martin Lippert
 */
public class SpringXMLLanguageServerComponents implements LanguageServerComponents {

	public static final Set<LanguageId> LANGUAGES = ImmutableSet.of(LanguageId.XML);

	private final JavaProjectFinder projectFinder;
	private final SpringXMLReconcileEngine reconcileEngine;
	private final DocumentSymbolHandler docSymbolProvider;

	public SpringXMLLanguageServerComponents(
			SimpleLanguageServer server,
			SpringSymbolIndex springIndexer,
			BootLanguageServerParams serverParams,
			BootJavaConfig config) {

		this.projectFinder = serverParams.projectFinder;
		this.docSymbolProvider = params -> springIndexer.getSymbols(params.getTextDocument().getUri());

		server.doOnInitialized(this::initialized);
		server.onShutdown(this::shutdown);

		this.reconcileEngine = new SpringXMLReconcileEngine(projectFinder);
		
		config.addListener(ignore -> {
			reconcileEngine.setSpelExpressionSyntaxValidationEnabled(config.isSpelExpressionValidationEnabled() && config.isSpringXMLSupportEnabled());
		});

	}

	@Override
	public Set<LanguageId> getInterestingLanguages() {
		return LANGUAGES;
	}

	@Override
	public Optional<IReconcileEngine> getReconcileEngine() {
		return Optional.of(this.reconcileEngine);
	}
	
	@Override
	public Optional<DocumentSymbolHandler> getDocumentSymbolProvider() {
		return Optional.of(docSymbolProvider);
	}

	@Override
	public HoverHandler getHoverProvider() {
		return null;
	}

	private void initialized() {
	}

	private void shutdown() {
	}

}
