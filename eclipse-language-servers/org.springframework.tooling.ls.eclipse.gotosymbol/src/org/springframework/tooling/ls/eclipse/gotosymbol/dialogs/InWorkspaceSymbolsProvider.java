/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.tooling.ls.eclipse.gotosymbol.GotoSymbolPlugin;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

@SuppressWarnings("restriction")
public class InWorkspaceSymbolsProvider implements SymbolsProvider {

	private static final Duration TIMEOUT = Duration.ofSeconds(2);
	
	private List<LanguageServer> languageServers;

	public InWorkspaceSymbolsProvider(List<LanguageServer> languageServers) {
		this.languageServers = languageServers;
	}

	@Override
	public String getName() {
		return "Symbols in Workspace";
	}

	@Override
	public Collection<SymbolInformation> fetchFor(String query) throws Exception {
		//TODO: if we want decent support for multiple language servers...
		// consider changing SymbolsProvider api and turning the stuff in here into something producing a 
		// Flux<Collection<SymbolInformation>>
		// This will help in 
		//  - supporting cancelation
		//  - executing multiple requests to different servers in parallel.
		//  - producing results per server so don't have to wait for one slow server to see the rest.
		//However it will also add complexity to the code that consumes this and at this time we only
		// really use this with a single language server anyways.
		ImmutableList.Builder<SymbolInformation> allSymbols = ImmutableList.builder();
		for (LanguageServer server : this.languageServers) {
			try {
				WorkspaceSymbolParams params = new WorkspaceSymbolParams(query);
				List<? extends SymbolInformation> symbolsFuture = 
						server.getWorkspaceService().symbol(params)
						.get(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
				allSymbols.addAll(symbolsFuture);
			} catch (Exception e) {
				GotoSymbolPlugin.getInstance().getLog().log(ExceptionUtil.status(e));
			}
		}
		return allSymbols.build();
	}
	
	public static InWorkspaceSymbolsProvider createFor(ExecutionEvent event) {
		IEditorPart part = HandlerUtil.getActiveEditor(event);
		IResource resource = null;
		if (part != null && part.getEditorInput() != null) {
			resource = part.getEditorInput().getAdapter(IResource.class);
		} else {
			IStructuredSelection selection = HandlerUtil.getCurrentStructuredSelection(event);
			if (selection.isEmpty() || !(selection.getFirstElement() instanceof IAdaptable)) {
				return null;
			}
			IAdaptable adaptable = (IAdaptable) selection.getFirstElement();
			resource = adaptable.getAdapter(IResource.class);
		}
		if (resource!=null) {
			return createFor(resource.getProject());
		}
		return null;
	}

	public static InWorkspaceSymbolsProvider createFor(IProject project) {
		List<LanguageServer> languageServers = LanguageServiceAccessor.getLanguageServers(project,
				capabilities -> Boolean.TRUE.equals(capabilities.getWorkspaceSymbolProvider()));
		if (!languageServers.isEmpty()) {
			return new InWorkspaceSymbolsProvider(languageServers);
		}
		return null;
	}

}
