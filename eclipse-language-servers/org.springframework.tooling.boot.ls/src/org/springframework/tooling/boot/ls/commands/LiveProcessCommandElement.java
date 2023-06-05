/*******************************************************************************
 * Copyright (c) 2019, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LiveProcessCommandElement extends QuickAccessElement {
	
	public static final String COMMAND_LIST_PROCESSES = "sts/livedata/listProcesses";
	public static final String COMMAND_CONNECT = "sts/livedata/connect";
	public static final String COMMAND_REFRESH = "sts/livedata/refresh";
	public static final String COMMAND_DISCONNECT = "sts/livedata/disconnect";
	
	private final String processKey;
	private final String label;
	private final String action;
	private final int randomIDExtension;

	public LiveProcessCommandElement(String processKey, String label, String action) {
		super();
		this.processKey = processKey;
		this.label = label;
		this.action = action;
		
		this.randomIDExtension = new Random().nextInt();
	}

	@Override
	public String getLabel() {
		if (COMMAND_REFRESH.equals(action)) {
			return "Refresh Live Data for: " + label;
		}
		else if (COMMAND_CONNECT.equals(action)) {
			return "Show live data for: " + label;
		}
		else if (COMMAND_DISCONNECT.equals(action)) {
			return "Disconnect live data from: " + label;
		}
		else {
			// error case
			return "No live data action avaiable for: " + label;
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getId() {
		return processKey + action + randomIDExtension;
	}

	@Override
	public void execute() {
		ExecuteCommandParams commandParams = new ExecuteCommandParams();
		commandParams.setCommand(this.action);
		
		List<Object> arguments = new ArrayList<>();
		Map<String, String> argumentMap = new HashMap<>();
		argumentMap.put("processKey", this.processKey);
		arguments.add(argumentMap);
		
		commandParams.setArguments(arguments);
		
		List<CompletableFuture<Object>> futures = LanguageServers
				.forProject(null)
				.excludeInactive()
				.withPreferredServer(LanguageServersRegistry.getInstance().getDefinition(BootLanguageServerPlugin.BOOT_LS_DEFINITION_ID))
				.computeAll(ls -> ls.getWorkspaceService().executeCommand(commandParams));

		try {
			CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(2, TimeUnit.SECONDS);
		}
		catch (Exception e) {
			// TODO: better exception handling
			e.printStackTrace();
		}
	}

}
