/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.livehover.v2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.ExecuteCommandParams;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author mlippert
 */
public class SpringProcessCommandHandler {
	
	private static final String COMMAND_LIST_PROCESSES = "sts/livedata/listProcesses";
	private static final String COMMAND_CONNECT = "sts/livedata/connect";
	private static final String COMMAND_REFRESH = "sts/livedata/refresh";
	private static final String COMMAND_DISCONNECT = "sts/livedata/disconnect";
	
	private final SpringProcessConnectorService connectorService;
	private final SpringProcessConnectorLocal localProcessConnector;

	public SpringProcessCommandHandler(SimpleLanguageServer server, SpringProcessConnectorService connectorService, SpringProcessConnectorLocal localProcessConnector) {
		this.connectorService = connectorService;
		this.localProcessConnector = localProcessConnector;

		server.onCommand(COMMAND_LIST_PROCESSES, (params) -> {
			return getProcessCommands();
		});
		
		server.onCommand(COMMAND_CONNECT, (params) -> {
			return connect(params);
		});
		
		server.onCommand(COMMAND_REFRESH, (params) -> {
			return refresh(params);
		});

		server.onCommand(COMMAND_DISCONNECT, (params) -> {
			return disconnect(params);
		});

	}

	private CompletableFuture<Object> connect(ExecuteCommandParams params) {
		String processKey = getProcessKey(params);
		if (processKey != null) {
			SpringProcessDescriptor[] processes = localProcessConnector.getProcesses();
			for (SpringProcessDescriptor process : processes) {
				if (process.getProcessKey().equals(processKey)) {
					localProcessConnector.connectLocalProcess(process, false);
					return CompletableFuture.completedFuture(null);
				}
			}
		}

		return CompletableFuture.completedFuture(null);
	}

	private CompletableFuture<Object> refresh(ExecuteCommandParams params) {
		String processKey = getProcessKey(params);
		if (processKey != null) {
			connectorService.refreshProcess(processKey);
		}

		return CompletableFuture.completedFuture(null);
	}

	private CompletableFuture<Object> disconnect(ExecuteCommandParams params) {
		return CompletableFuture.completedFuture(null);
	}

	private CompletableFuture<Object> getProcessCommands() {
		List<LiveProcessCommand> result = new ArrayList<>();
		
		Set<String> alreadyConnected = new HashSet<>();

		// already connected processes
		SpringProcessConnector[] connectedProcesses = connectorService.getConnectedProcesses();
		for (SpringProcessConnector process : connectedProcesses) {
			String processKey = process.getProcessKey();
			String label = process.getLabel();
			String action = COMMAND_REFRESH;

			LiveProcessCommand command = new LiveProcessCommand();
			command.setAction(action);
			command.setLabel(label);
			command.setProcessKey(processKey);
			
			result.add(command);
			
			alreadyConnected.add(processKey);
		}
		
		// other available local processes
		SpringProcessDescriptor[] localProcesses = localProcessConnector.getProcesses();
		for (SpringProcessDescriptor localProcess : localProcesses) {
			String processKey = localProcess.getProcessKey();
			if (!alreadyConnected.contains(processKey)) {
				String label = localProcess.getLabel();
				String action = COMMAND_CONNECT;

				LiveProcessCommand command = new LiveProcessCommand();
				command.setAction(action);
				command.setLabel(label);
				command.setProcessKey(processKey);
				
				result.add(command);
			}
		}
		
		return CompletableFuture.completedFuture((Object[]) result.toArray(new Object[result.size()]));
	}
	
	private String getProcessKey(ExecuteCommandParams params) {
		List<Object> arguments = params.getArguments();
		for (Object arg : arguments) {
			if (arg instanceof Map<?, ?>) {
				Object value = ((Map<?, ?>) arg).get("processKey");
				if (value != null) {
					return value.toString();
				}
			}
			else if (arg instanceof JsonObject) {
				JsonElement element = ((JsonObject) arg).get("processKey");
				if (element != null) {
					return element.getAsString();
				}
			}
		}
		
		return null;
	}

}
