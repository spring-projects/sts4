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

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.AsyncRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import reactor.core.Disposable;

public class ClasspathListenerManager {

	private static Logger log = LoggerFactory.getLogger(ClasspathListenerManager.class);

	private static final String WORKSPACE_EXECUTE_COMMAND = "workspace/executeCommand";
	private SimpleLanguageServer server;
	private AsyncRunner async;

	private static final Gson gson = new Gson();

	public ClasspathListenerManager(SimpleLanguageServer server) {
		this.server = server;
		this.async = server.getAsync();
	}

	public Disposable addClasspathListener(ClasspathListener classpathListener) {
		String callbackCommandId = "sts4.classpath." + RandomStringUtils.randomAlphabetic(8);

		// 1. register callback command handler in SimpleLanguageServer
		Disposable unregisterCommand = server.onCommand(callbackCommandId, (ExecuteCommandParams callbackParams) -> async.invoke(() -> {
			log.debug("callback {} received {}", callbackCommandId, callbackParams);
			List<Object> args = callbackParams.getArguments();
			log.debug("args = {}", args);
			//Note: not sure... but args might be deserialized as com.google.gson.JsonElement's.
			//If so the code below is not correct (casts will fail).
			String projectUri = ((JsonElement) args.get(0)).getAsString();
			log.debug("projectUri = {}", args);
			String name = ((JsonElement) args.get(1)).getAsString();
			log.debug("name = {}", args);
			boolean deleted = ((JsonElement)args.get(2)).getAsBoolean();
			log.debug("deleted = {}", deleted);

			Classpath classpath = gson.fromJson((JsonElement)args.get(3), Classpath.class);
			log.debug("classpath = {}", classpath);

			classpathListener.changed(new ClasspathListener.Event(projectUri, name, deleted, classpath));
			return "done";
		}));

		// 2. register the callback command with the client
		String registrationId = UUID.randomUUID().toString();
		RegistrationParams params = new RegistrationParams(ImmutableList.of(
				new Registration(registrationId,
						WORKSPACE_EXECUTE_COMMAND,
						ImmutableMap.of("commands", ImmutableList.of(callbackCommandId))
				)
		));
		server.getClient().registerCapability(params).join();

		// 3. call the client to ask it to call that callback
		server.getClient().addClasspathListener(
				new ClasspathListenerParams(callbackCommandId)
		).join();

		// Cleanups:
		return () -> {
			try {
				log.info("Unregistering classpath callback "+callbackCommandId +" ...");
				this.server.getClient().removeClasspathListener(
						new ClasspathListenerParams(callbackCommandId)
				).join();
				log.info("Unregistering classpath callback "+callbackCommandId +" OK");
				this.server.getClient().unregisterCapability(new UnregistrationParams(ImmutableList.of(
						new Unregistration(registrationId, WORKSPACE_EXECUTE_COMMAND)
				))).join();
				unregisterCommand.dispose();
			} catch (Exception e) {
				log.error("", e);
			}
		};
	}

}
