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
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessConnectorRemote.RemoteBootAppData;
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
	private final SpringProcessConnectorRemote remoteProcessConnector;

	public SpringProcessCommandHandler(SimpleLanguageServer server, SpringProcessConnectorService connectorService,
			SpringProcessConnectorLocal localProcessConnector, SpringProcessConnectorRemote remoteProcessConnector) {
		this.connectorService = connectorService;
		this.localProcessConnector = localProcessConnector;
		this.remoteProcessConnector = remoteProcessConnector;

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
			
			// try local processes
			SpringProcessDescriptor[] processes = localProcessConnector.getProcesses(false, SpringProcessStatus.REGULAR);
			for (SpringProcessDescriptor process : processes) {
				if (process.getProcessKey().equals(processKey)) {
					localProcessConnector.connectProcess(process);
					return CompletableFuture.completedFuture(null);
				}
			}
			
			// try remote processes
			RemoteBootAppData[] remoteProcesses = remoteProcessConnector.getProcesses();
			for (RemoteBootAppData remoteProcess : remoteProcesses) {
				String key = SpringProcessConnectorRemote.getProcessKey(remoteProcess);
				if (processKey.equals(key)) {
					remoteProcessConnector.connectProcess(remoteProcess);
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
		String processKey = getProcessKey(params);
		if (processKey != null) {
			connectorService.disconnectProcess(processKey);
		}

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

			LiveProcessCommand refreshCommand = new LiveProcessCommand();
			refreshCommand.setAction(COMMAND_REFRESH);
			refreshCommand.setLabel(label);
			refreshCommand.setProcessKey(processKey);
			result.add(refreshCommand);
			
			LiveProcessCommand disconnectCommand = new LiveProcessCommand();
			disconnectCommand.setAction(COMMAND_DISCONNECT);
			disconnectCommand.setLabel(label);
			disconnectCommand.setProcessKey(processKey);
			result.add(disconnectCommand);

			alreadyConnected.add(processKey);
		}
		
		// other available local processes
		SpringProcessDescriptor[] localProcesses = localProcessConnector.getProcesses(true, SpringProcessStatus.REGULAR);
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
		
		// other available remote processes
		RemoteBootAppData[] remoteProcesses = remoteProcessConnector.getProcesses();
		for (RemoteBootAppData remoteProcess : remoteProcesses) {
			String processKey = SpringProcessConnectorRemote.getProcessKey(remoteProcess);
			if (!alreadyConnected.contains(processKey)) {
				String label = "remote process: " + remoteProcess.getJmxurl();
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
