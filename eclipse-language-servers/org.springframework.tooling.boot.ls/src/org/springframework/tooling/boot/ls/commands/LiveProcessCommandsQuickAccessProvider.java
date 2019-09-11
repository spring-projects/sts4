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
package org.springframework.tooling.boot.ls.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class LiveProcessCommandsQuickAccessProvider implements IQuickAccessComputer, IQuickAccessComputerExtension {

	private List<@NonNull LanguageServer> usedLanguageServers;

	@Override
	public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[0];
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return this.usedLanguageServers == null;
	}

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		this.usedLanguageServers = LanguageServiceAccessor.getActiveLanguageServers(serverCapabilities -> true);

		if (usedLanguageServers.isEmpty()) {
			return new QuickAccessElement[0];
		}
		
		ExecuteCommandParams commandParams = new ExecuteCommandParams();
		commandParams.setCommand(LiveProcessCommandElement.COMMAND_LIST_PROCESSES);

		List<QuickAccessElement> res = Collections.synchronizedList(new ArrayList<>());
		
		try {
			CompletableFuture.allOf(usedLanguageServers.stream().map(ls ->
			ls.getWorkspaceService().executeCommand(commandParams).thenAcceptAsync(commandResult ->
				createCommandItems(res, commandResult))).toArray(CompletableFuture[]::new)).get(2000, TimeUnit.MILLISECONDS);
		}
		catch (Exception e) {
			// TODO: better error handling
			e.printStackTrace();
		}
		
		return res.toArray(new QuickAccessElement[res.size()]);
	}

	private void createCommandItems(List<QuickAccessElement> res, Object commandResult) {
		System.out.println(commandResult);
		
		if (commandResult instanceof List<?>) {
			List<?> allCommands = (List<?>) commandResult;
			for (Object command : allCommands) {
				if (command instanceof Map<?, ?>) {
					res.add(createCommandItem((Map<?, ?>)command));
				}
			}
		}
	}

	private LiveProcessCommandElement createCommandItem(Map<?, ?> command) {
		String processKey = (String) command.get("processKey");
		String label = (String) command.get("label");
		String action = (String) command.get("action");
		
		return new LiveProcessCommandElement(processKey, label, action);
	}

}
