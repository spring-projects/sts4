/*******************************************************************************
 * Copyright (c) 2019, 2022 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.liveprocess;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("restriction")
public final class DefaultLiveProcessCommandExecutor implements LiveProcessCommandsExecutor {

	private static final String CMD_LIST_PROCESSES = "sts/livedata/listProcesses";

	private class DefaultServer implements Server {
		private LanguageServer ls;
		public DefaultServer(LanguageServer ls) {
			this.ls = ls;
		}
		@Override
		@SuppressWarnings("unchecked")
		public Flux<CommandInfo> listCommands() {
			return Mono.fromFuture(ls.getWorkspaceService().executeCommand(new ExecuteCommandParams(
					CMD_LIST_PROCESSES,
					ImmutableList.of()
			)))
			.flatMapIterable(list -> (List<?>)list)
			.map(_cmdInfo -> {
				Map<String,String> map = (Map<String, String>) _cmdInfo;
				return new CommandInfo(map.get("action"), map);
			});
		}

		@Override
		public Mono<Void> executeCommand(CommandInfo cmd) {
			return Mono.fromRunnable(() -> ls.getWorkspaceService().executeCommand(new ExecuteCommandParams(
						cmd.command,
						ImmutableList.of(cmd.info)
			)));
		}
	}

	@Override
	public List<Server> getLanguageServers() {
		return LanguageServiceAccessor.getActiveLanguageServers(cap -> {
			ExecuteCommandOptions commandCap = cap.getExecuteCommandProvider();
			if (commandCap!=null) {
				List<String> supportedCommands = commandCap.getCommands();
				return supportedCommands!=null && supportedCommands.contains(CMD_LIST_PROCESSES);
			}
			return false;
		})
		.stream()
		.map(DefaultServer::new)
		.collect(Collectors.toList());
	}
}