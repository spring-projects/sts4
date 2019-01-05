/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gayan Perera <gayanper@gmail.com> - In project symbol provider implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;

@SuppressWarnings("restriction")
public class InProjectSymbolsProvider extends InWorkspaceSymbolsProvider {

	private final Predicate<? super Either<SymbolInformation, DocumentSymbol>> FILTER_PREDICATE = e -> {
		if(e.isLeft()) {
			SymbolInformation symbolInformation = e.getLeft();
			Location location = symbolInformation.getLocation();
			IResource targetResource = LSPEclipseUtils.findResourceFor(location.getUri());
			if (targetResource != null && targetResource.getFullPath() != null) {
				return targetResource.getFullPath().toString().startsWith("/" + getProject().getName() + "/");			
			}
		}
		return false;
	};

	public InProjectSymbolsProvider(List<LanguageServer> languageServers, IProject project) {
		super(languageServers, project);
	}
	
	@Override
	public String getName() {
		return "Symbols in Project";
	}

	public static InWorkspaceSymbolsProvider createFor(ExecutionEvent event) {
		final IProject project = InWorkspaceSymbolsProvider.projectFor(event);
		final List<LanguageServer> languageServers = LanguageServiceAccessor.getLanguageServers(project,
				capabilities -> Boolean.TRUE.equals(capabilities.getWorkspaceSymbolProvider()), true);
		if (!languageServers.isEmpty()) {
			return new InProjectSymbolsProvider(languageServers, project);
		}
		return null;
	}
	
	@Override
	protected Predicate<? super Either<SymbolInformation, DocumentSymbol>> symbolFilter() {
		return FILTER_PREDICATE;
	}
	
}
