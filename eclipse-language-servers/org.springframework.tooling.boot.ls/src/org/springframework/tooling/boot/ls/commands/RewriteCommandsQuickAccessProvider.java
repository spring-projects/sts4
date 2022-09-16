/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.commands;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

@SuppressWarnings("restriction")
public class RewriteCommandsQuickAccessProvider implements IQuickAccessComputer, IQuickAccessComputerExtension {

	private static final String CMD_REWRITE_RELOAD = "sts/rewrite/reload";

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		return new QuickAccessElement[] {
			new QuickAccessElement() {
				
				@Override
				public String getLabel() {
					return "Reload Rewrite Recipes";
				}
				
				@Override
				public ImageDescriptor getImageDescriptor() {
					return null;
				}
				
				@Override
				public String getId() {
					return CMD_REWRITE_RELOAD;
				}
				
				@Override
				public void execute() {
					List<LanguageServer> usedLanguageServers = LanguageServiceAccessor.getActiveLanguageServers(serverCapabilities -> true);

					if (usedLanguageServers.isEmpty()) {
						return;
					}
					
					ExecuteCommandParams commandParams = new ExecuteCommandParams();
					commandParams.setCommand(CMD_REWRITE_RELOAD);
					
					commandParams.setArguments(Collections.emptyList());

					try {
						CompletableFuture.allOf(usedLanguageServers.stream().map(ls ->
							ls.getWorkspaceService().executeCommand(commandParams)).toArray(CompletableFuture[]::new)).get(2, TimeUnit.SECONDS);
					}
					catch (Exception e) {
						BootLanguageServerPlugin.getDefault().getLog().error("Failed to reload Rewrite Recipes!", e);
					}
				}
			}
		};
	}

	@Override
	public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[0];
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return false;
	}

}
