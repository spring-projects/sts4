/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
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
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.springframework.tooling.jdt.ls.commons.classpath.ClientCommandExecutor;

@SuppressWarnings("restriction")
public class LSP4ECommandExecutor implements ClientCommandExecutor {

	@Override
	public Object executeClientCommand(String id, Object... params) throws Exception {
		Optional<Object> res = LanguageServers.forProject(null).withFilter(handlesCommand(id)).computeFirst(
				ls -> ls.getWorkspaceService().executeCommand(new ExecuteCommandParams(id, Arrays.asList(params))))
				.get(2, TimeUnit.SECONDS);
		return res.orElseThrow(() -> new UnsupportedOperationException(
				"No language server has registered to handle command '" + id + "'"));
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
