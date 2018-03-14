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

import java.util.UUID;

import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import reactor.core.Disposable;

public class ClasspathListenerManager {

	private static final String WORKSPACE_EXECUTE_COMMAND = "workspace/executeCommand";
	private int commandIdCounter = 0;
	private SimpleLanguageServer server;

	public ClasspathListenerManager(SimpleLanguageServer server) {
		this.server = server;
	}

	public Disposable addClasspathListener(ClasspathListener classpathListener) {
		// TODO:
		// 1. register callback command handler in SimpleLanguageServer
		// 2. call the client to ask it to call that callback
		// 3. register the callback command with the client
		String callbackCommandId = "sts4.classpath." + (commandIdCounter++);
		String registrationId = UUID.randomUUID().toString();

		RegistrationParams params = new RegistrationParams(ImmutableList.of(
				new Registration(registrationId,
						WORKSPACE_EXECUTE_COMMAND,
						ImmutableMap.of("commands", ImmutableList.of(callbackCommandId))
				)
			));
		server.getClient().registerCapability(params);
		return () -> {
			this.server.getClient().unregisterCapability(new UnregistrationParams(ImmutableList.of(
					new Unregistration(registrationId, WORKSPACE_EXECUTE_COMMAND)
				)));
		};
	}

}
