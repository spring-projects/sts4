/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.tooling.jdt.ls.commons.classpath.ClientCommandExecutor;

@SuppressWarnings("restriction")
public class LSP4ECommandExecutor implements ClientCommandExecutor {

	@Override
	public Object executeClientCommand(String id, Object... params) throws Exception {
		List<LanguageServer> commandHandlers = LanguageServiceAccessor.getActiveLanguageServers(handlesCommand(id));
		if (commandHandlers != null) {
			if (commandHandlers.size() == 1) {
				LanguageServer handler = commandHandlers.get(0);
				return handler
						.getWorkspaceService()
						.executeCommand(new ExecuteCommandParams(id, Arrays.asList(params)))
						.get(2, TimeUnit.SECONDS);
			} else if (commandHandlers.size() > 1) {
				throw new IllegalStateException("Multiple language servers have registered to handle command '"+id+"'");
			}
		}
		throw new UnsupportedOperationException("No language server has registered to handle command '"+id+"'");
	}

	private Predicate<ServerCapabilities> handlesCommand(String id) {
		return (serverCaps) -> {
			ExecuteCommandOptions executeCommandProvider = serverCaps.getExecuteCommandProvider();
			if (executeCommandProvider != null) {
				return executeCommandProvider.getCommands().contains(id);
			}
			return false;
		};
	}

}
