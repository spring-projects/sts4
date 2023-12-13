/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
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
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.ui.quickaccess.IQuickAccessComputer;
import org.eclipse.ui.quickaccess.IQuickAccessComputerExtension;
import org.eclipse.ui.quickaccess.QuickAccessElement;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

@SuppressWarnings("restriction")
public class SpringQuickAccessProvider implements IQuickAccessComputer, IQuickAccessComputerExtension {
	
	final private QuickAccessElement reloadPropertiesAction = createForNoArgsLsCommand(
			"sts/common-properties/reload",
			"Reload Shared Properties Metadata",
			"Failed to reload shared properties metadata file!"
	);
	
	final private QuickAccessElement reloadRecipesAction = createForNoArgsLsCommand(
			"sts/rewrite/reload",
			"Reload OpenRewrite Recipes",
			"Failed to reload OpenRewrite Recipes!"
	);
	
	private static QuickAccessElement createForNoArgsLsCommand(String commandId, String label, String errorMsg) {
		return new QuickAccessElement() {
			
			@Override
			public String getLabel() {
				return label;
			}
			
			@Override
			public ImageDescriptor getImageDescriptor() {
				return null;
			}
			
			@Override
			public String getId() {
				return commandId;
			}
			
			@Override
			public void execute() {
				ExecuteCommandParams commandParams = new ExecuteCommandParams();
				commandParams.setCommand(commandId);
				
				commandParams.setArguments(Collections.emptyList());
				
				List<CompletableFuture<Object>> futures = LanguageServers
						.forProject(null)
						.excludeInactive()
						.withPreferredServer(LanguageServersRegistry.getInstance().getDefinition(BootLanguageServerPlugin.BOOT_LS_DEFINITION_ID))
						.computeAll(ls -> ls.getWorkspaceService().executeCommand(commandParams));

				try {
					CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).get(2, TimeUnit.SECONDS);
				}
				catch (Exception e) {
					BootLanguageServerPlugin.getDefault().getLog().error(errorMsg, e);
				}
			}
		};
	}

	@Override
	public QuickAccessElement[] computeElements() {
		return new QuickAccessElement[] {
				reloadPropertiesAction,
				reloadRecipesAction
		};
	}

	@Override
	public void resetState() {
	}

	@Override
	public boolean needsRefresh() {
		return false;
	}

	@Override
	public QuickAccessElement[] computeElements(String query, IProgressMonitor monitor) {
		return new QuickAccessElement[] {
				reloadPropertiesAction,
				reloadRecipesAction
		};
	}

}
