/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.jdt.ls;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.springframework.ide.vscode.commons.languageserver.util.AsyncRunner;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;

import reactor.core.Disposable;

public class ClasspathListenerManager {

	private static final String WORKSPACE_EXECUTE_COMMAND = "workspace/executeCommand";
	private int commandIdCounter = 0;
	private SimpleLanguageServer server;
	private AsyncRunner async;

	public ClasspathListenerManager(SimpleLanguageServer server) {
		this.server = server;
		this.async = server.getAsync();
	}

	public Disposable addClasspathListener(ClasspathListener classpathListener) {
		String callbackCommandId = "sts4.classpath." + (commandIdCounter++);
		// 1. register callback command handler in SimpleLanguageServer
		Disposable unregisterCommand = server.onCommand(callbackCommandId, (ExecuteCommandParams callbackParams) -> async.invoke(() -> {
			List<Object> args = callbackParams.getArguments();
			//Note: not sure... but args might be deserialized as com.google.gson.JsonElement's.
			//If so the code below is not correct (casts will fail).
			String projectUri = ((JsonElement) args.get(0)).getAsString();
			boolean deleted = args.size()>=2 && ((JsonElement)args.get(1)).getAsBoolean();
			classpathListener.changed(projectUri, deleted);
			return "done";
		}));

		// 2. call the client to ask it to call that callback
		CompletableFuture<ClasspathListenerResponse> future1 = server.getClient().addClasspathListener(new ClasspathListenerParams(callbackCommandId));

		// 2. register the callback command with the client
		String registrationId = UUID.randomUUID().toString();
		RegistrationParams params = new RegistrationParams(ImmutableList.of(
				new Registration(registrationId,
						WORKSPACE_EXECUTE_COMMAND,
						ImmutableMap.of("commands", ImmutableList.of(callbackCommandId))
				)
		));
		CompletableFuture<Void> future2 = server.getClient().registerCapability(params);

		// Wait for async work
		//future1.join();
		//future2.join();

		// Cleanups:
		return () -> {
			unregisterCommand.dispose();
			this.server.getClient().unregisterCapability(new UnregistrationParams(ImmutableList.of(
					new Unregistration(registrationId, WORKSPACE_EXECUTE_COMMAND)
			)));
		};
	}

}
