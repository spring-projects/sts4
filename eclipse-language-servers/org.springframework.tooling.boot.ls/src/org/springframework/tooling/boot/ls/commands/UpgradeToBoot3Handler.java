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

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class UpgradeToBoot3Handler extends AbstractHandler {

	private static final String UPGRADE_TO_BOOT_3_COMMAND_ID = "sts/rewrite/recipe/org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
		Object o = selection.getFirstElement();
		IProject project = null;
		if (o instanceof IResource) {
			project = ((IResource) o).getProject();
		} else if (o instanceof IProject) {
			project = (IProject) o;
		} else if (o instanceof IAdaptable) {
			project = ((IAdaptable) o).getAdapter(IProject.class);
		}
		if (project != null) {
			List<@NonNull LanguageServer> usedLanguageServers = LanguageServiceAccessor
					.getActiveLanguageServers(serverCapabilities -> true);

			if (!usedLanguageServers.isEmpty()) {
				ExecuteCommandParams commandParams = new ExecuteCommandParams();
				commandParams.setCommand(UPGRADE_TO_BOOT_3_COMMAND_ID);
				commandParams.setArguments(List.of(project.getLocationURI().toString()));

				try {
					CompletableFuture.allOf(usedLanguageServers.stream()
							.map(ls -> ls.getWorkspaceService().executeCommand(commandParams))
							.toArray(CompletableFuture[]::new));
				} catch (Exception e) {
					Log.log(e);
					throw new ExecutionException("Failed to perform Upgarde to Spring Boot 3", e);
				}
			}

		}
		return null;
	}

}
