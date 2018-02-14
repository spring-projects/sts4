/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.composable;

import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.VscodeCompletionEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.HoverInfoProvider;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter;
import org.springframework.ide.vscode.commons.languageserver.hover.VscodeHoverEngineAdapter.HoverType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServerWrapper;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * Facilitates composition of language servers from composable {@link LanguageServerComponents}.
 *
 * @author Kris De Volder
 */
public class ComposableLanguageServer<C extends LanguageServerComponents> implements SimpleLanguageServerWrapper {

	private final VscodeHoverEngineAdapter hoverEngine;
	private final SimpleLanguageServer server;
	private C components;
	private VscodeCompletionEngineAdapter completionEngineAdapter;

	public ComposableLanguageServer(String extensionId, LSFactory<C> _components) {
		this.server = new SimpleLanguageServer(extensionId);
		this.components = _components.create(server);

		SimpleTextDocumentService documents = server.getTextDocumentService();

		IReconcileEngine reconcileEngine = components.getReconcileEngine();
		if (reconcileEngine!=null) {
			documents.onDidChangeContent(params -> {
				TextDocument doc = params.getDocument();
				server.validateWith(doc.getId(), reconcileEngine);
			});
		}

		ICompletionEngine completionEngine = components.getCompletionEngine();
		if (completionEngine!=null) {
			completionEngineAdapter = server.createCompletionEngineAdapter(server, completionEngine);
			completionEngineAdapter.setMaxCompletions(100);
			documents.onCompletion(completionEngineAdapter::getCompletions);
			documents.onCompletionResolve(completionEngineAdapter::resolveCompletion);
		}

		HoverInfoProvider hoverInfoProvider = components.getHoverProvider();
		hoverEngine = new VscodeHoverEngineAdapter(server, hoverInfoProvider);
		documents.onHover(hoverEngine::getHover);
	}

	public C getComponents() {
		return components;
	}

	public void setMaxCompletionsNumber(int number) {
		if (completionEngineAdapter!=null) {
			completionEngineAdapter.setMaxCompletions(number);
		}
	}

	public void setHoverType(HoverType type) {
		hoverEngine.setHoverType(type);
	}

	@Override
	public SimpleLanguageServer getServer() {
		return this.server;
	}
}
