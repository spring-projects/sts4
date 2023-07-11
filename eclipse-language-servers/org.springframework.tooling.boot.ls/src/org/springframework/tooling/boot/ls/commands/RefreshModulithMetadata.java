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

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServersRegistry.LanguageServerDefinition;
import org.eclipse.lsp4j.ExecuteCommandParams;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

@SuppressWarnings("restriction")
public class RefreshModulithMetadata extends AbstractHandler {

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
			LanguageServerDefinition def = LanguageServersRegistry.getInstance().getDefinition(BootLanguageServerPlugin.BOOT_LS_DEFINITION_ID);
			Assert.isLegal(def != null, "No definition found for Boot Language Server");

			final String uri = project.getLocationURI().toASCIIString();
			ExecuteCommandParams commandParams = new ExecuteCommandParams();
			commandParams.setCommand("sts/modulith/metadata/refresh");
			commandParams.setArguments(List.of(uri));
			
			LanguageServers.forProject(project).withPreferredServer(def).computeFirst(ls -> ls.getWorkspaceService().executeCommand(commandParams));
		}
		return null;
	}

}
