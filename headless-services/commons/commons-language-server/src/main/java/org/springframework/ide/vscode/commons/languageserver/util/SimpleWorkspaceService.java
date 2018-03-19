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
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;

import reactor.core.publisher.Mono;

public class SimpleWorkspaceService implements WorkspaceService {

	private static Logger log = LoggerFactory.getLogger(SimpleWorkspaceService.class);

	private SimpleLanguageServer server;
	private Set<WorkspaceFolder> workspaceRoots = new HashSet<>();

	private ListenerList<Settings> configurationListeners = new ListenerList<>();
	private ExecuteCommandHandler executeCommandHandler;
	private WorkspaceSymbolHandler workspaceSymbolHandler;
	private SimpleServerFileObserver fileObserver;

	private ListenerList<DidChangeWorkspaceFoldersParams> workspaceFolderListeners = new ListenerList<>();

	public SimpleWorkspaceService(SimpleLanguageServer server) {
		this.server = server;
		this.fileObserver = new SimpleServerFileObserver(server);
	}

	@Override
	public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams params) {
		WorkspaceSymbolHandler workspaceSymbolHandler = this.workspaceSymbolHandler;
		if (workspaceSymbolHandler==null) {
			return CompletableFuture.completedFuture(ImmutableList.of());
		}
		return Mono.fromCallable(() -> {
			server.waitForReconcile();
			List<? extends SymbolInformation> symbols = workspaceSymbolHandler.handle(params);
			return symbols==null ? ImmutableList.of() : symbols;
		})
		.toFuture()
		.thenApply(l -> (List<? extends SymbolInformation>)l);
	}

	@Override
	public void didChangeConfiguration(DidChangeConfigurationParams params) {
		configurationListeners.fire(new Settings((JsonElement) params.getSettings()));
	}

	@Override
	public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
		params.getChanges().forEach(event -> {
			try {
				String uri = event.getUri();
				if (uri != null) {
					switch (event.getType()) {
					case Created:
						fileObserver.notifyFileCreated(uri);
						break;
					case Changed:
						fileObserver.notifyFileChanged(uri);
						break;
					case Deleted:
						fileObserver.notifyFileDeleted(uri);
						break;
					default:
						Log.log("Uknown file change type '" + event.getType() + "' for file: " + uri);
						break;
					}
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		});
	}

	@Override
	public synchronized void didChangeWorkspaceFolders(DidChangeWorkspaceFoldersParams params) {
		WorkspaceFoldersChangeEvent evt = params.getEvent();
		boolean changed = false;
		for (WorkspaceFolder r : evt.getAdded()) {
			workspaceRoots.add(r);
			changed = true;
		}
		for (WorkspaceFolder r : evt.getRemoved()) {
			workspaceRoots.remove(r);
			changed = true;
		}
		if (changed) {
			workspaceFolderListeners.fire(params);
		}
	}

	public void onDidChangeWorkspaceFolders(Consumer<DidChangeWorkspaceFoldersParams> l) {
		workspaceFolderListeners.add(l);
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

	public synchronized void onWorkspaceSymbol(WorkspaceSymbolHandler h) {
		Assert.isNull("A WorkspaceSymbolHandler is already set, multiple handlers not supported yet", workspaceSymbolHandler);
		this.workspaceSymbolHandler = h;
	}

	public boolean hasWorkspaceSymbolHandler() {
		return this.workspaceSymbolHandler != null;
	}

	public FileObserver getFileObserver() {
		return fileObserver;
	}

	public void dispose() {
		fileObserver.dispose();
	}

	public Collection<WorkspaceFolder> getWorkspaceRoots() {
		return ImmutableList.copyOf(workspaceRoots);
	}

	public synchronized void setWorkspaceFolders(List<WorkspaceFolder> workspaceFolders) {
		workspaceRoots = new HashSet<>();
		workspaceRoots.addAll(workspaceFolders);
		log.debug("workspaceFolders = {}", workspaceFolders);
	}

}
