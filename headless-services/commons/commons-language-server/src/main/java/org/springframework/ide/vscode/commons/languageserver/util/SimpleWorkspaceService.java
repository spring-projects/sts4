/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.springframework.ide.vscode.commons.util.Assert;

public class SimpleWorkspaceService implements WorkspaceService {

	private ListenerList<Settings> configurationListeners = new ListenerList<>();
	private ExecuteCommandHandler executeCommandHandler;

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		configurationListeners.fire(new Settings(params.getSettings()));
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
	}

	@Override
	public CompletableFuture<Object> executeCommand(ExecuteCommandParams params) {
		if (this.executeCommandHandler!=null) {
			return this.executeCommandHandler.handle(params);
		}
		throw new UnsupportedOperationException();
	}

	public void onDidChangeConfiguraton(Consumer<Settings> l) {
		configurationListeners.add(l);
	}

	public void onExecuteCommand(ExecuteCommandHandler handler) {
		Assert.isNull("A executeCommandHandler is already set, multiple handlers not supported yet", this.executeCommandHandler);
		this.executeCommandHandler = handler;
	}
}
