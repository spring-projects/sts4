/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.composable.LanguageServerComponents;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.HoverHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.LanguageId;

import com.google.common.collect.ImmutableSet;

/**
 * @author Martin Lippert
 */
public class SpringXMLLanguageServerComponents implements LanguageServerComponents {

	public static final Set<LanguageId> LANGUAGES = ImmutableSet.of(LanguageId.XML);

	private static final Logger log = LoggerFactory.getLogger(SpringXMLLanguageServerComponents.class);

	private final SimpleLanguageServer server;
	private final BootLanguageServerParams serverParams;
	private final JavaProjectFinder projectFinder;
	private final SpringSymbolIndex symbolIndex;
	private final SpringXMLCompletionEngine completionEngine;
	private final SpringXMLReconcileEngine reconcileEngine;


	public SpringXMLLanguageServerComponents(
			SimpleLanguageServer server,
			SpringSymbolIndex springIndexer,
			BootLanguageServerParams serverParams,
			BootJavaConfig config) {

		this.server = server;
		this.serverParams = serverParams;
		this.projectFinder = serverParams.projectFinder;
		this.symbolIndex = springIndexer;

		server.doOnInitialized(this::initialized);
		server.onShutdown(this::shutdown);

		this.completionEngine = new SpringXMLCompletionEngine(this, server, projectFinder, symbolIndex, config);
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
	public ICompletionEngine getCompletionEngine() {
		return this.completionEngine;
	}
	
	@Override
	public Optional<IReconcileEngine> getReconcileEngine() {
		return Optional.of(this.reconcileEngine);
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
