/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.springframework.ide.vscode.commons.languageserver.json.DidChangeWatchedFilesRegistrationOptions;
import org.springframework.ide.vscode.commons.languageserver.json.FileSystemWatcher;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

/**
 * File Observer implementation for a language server. Able to register listeners to specific files via LS protocol
 *
 * @author Alex Boyko
 *
 */
public class SimpleServerFileObserver extends BasicFileObserver {

	private static final String WORKSPACE_DID_CHANGE_WATCHED_FILES = "workspace/didChangeWatchedFiles";

	private SimpleLanguageServer server;

	public SimpleServerFileObserver(SimpleLanguageServer server) {
		this.server = server;
	}

	@Override
	public String onFileCreated(List<String> globPattern, Consumer<String> handler) {
		String subscriptionId = super.onFileCreated(globPattern, handler);
		subscribe(subscriptionId, globPattern, FileSystemWatcher.CREATE);
		return subscriptionId;
	}

	@Override
	public String onFileChanged(List<String> globPattern, Consumer<String> handler) {
		String subscriptionId = super.onFileChanged(globPattern, handler);
		subscribe(subscriptionId, globPattern, FileSystemWatcher.CHANGE);
		return subscriptionId;
	}

	@Override
	public String onFileDeleted(List<String> globPattern, Consumer<String> handler) {
		String subscriptionId = super.onFileDeleted(globPattern, handler);
		subscribe(subscriptionId, globPattern, FileSystemWatcher.DELETE);
		return subscriptionId;
	}

	private void subscribe(String subscriptionId, List<String> globPattern, int kind) {
		server.onInitialized(() -> {
			if (server.canRegisterFileWatchersDynamically()) {
				List<FileSystemWatcher> watchers = globPattern.stream().map(pattern -> new FileSystemWatcher(pattern, kind)).collect(Collectors.toList());
				Registration registration = new Registration(subscriptionId, WORKSPACE_DID_CHANGE_WATCHED_FILES, new DidChangeWatchedFilesRegistrationOptions(watchers));
				server.getClient().registerCapability(new RegistrationParams(Arrays.asList(registration)));
			}
		});
	}

	@Override
	public boolean unsubscribe(String subscriptionId) {
		server.onInitialized(() -> {
			if (server.canRegisterFileWatchersDynamically()) {
				server.getClient().unregisterCapability(new UnregistrationParams(Arrays.asList(new Unregistration(subscriptionId, WORKSPACE_DID_CHANGE_WATCHED_FILES))));
			}
		});
		return super.unsubscribe(subscriptionId);
	}

	private void unsubscribeAll(Collection<String> subscriptions) {
		List<Unregistration> unregisterations = subscriptions.stream().map(s -> new Unregistration(s, WORKSPACE_DID_CHANGE_WATCHED_FILES)).collect(Collectors.toList());
		if (!unregisterations.isEmpty()) {
			server.getClient().unregisterCapability(new UnregistrationParams(unregisterations));
		}
	}

	void dispose() {
		if (server.canRegisterFileWatchersDynamically()) {
			unsubscribeAll(createRegistry.keySet());
			unsubscribeAll(changeRegistry.keySet());
			unsubscribeAll(deleteRegistry.keySet());
		}
	}

}
