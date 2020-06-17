/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Gayan Perera <gayanper@gmail.com> - In project symbol provider implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IProject;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageServer;
import org.springframework.tooling.ls.eclipse.gotosymbol.GotoSymbolPlugin;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SuppressWarnings("restriction")
public class InProjectSymbolsProvider implements SymbolsProvider {

	public static InProjectSymbolsProvider createFor(LiveExpression<IProject> project) {
		LiveExpression<List<LanguageServer>> languageServers = project.apply(p -> {
			System.out.println("project = "+(p==null?null:p.getName()));
			return p == null 
				? ImmutableList.of()
				: LanguageServiceAccessor.getLanguageServers(
					project.getValue(), 
					capabilities -> Boolean.TRUE.equals(capabilities.getWorkspaceSymbolProvider()), 
					true
				);
		});
		return new InProjectSymbolsProvider(languageServers::getValue, project::getValue);
	}

	public static InProjectSymbolsProvider createFor(ExecutionEvent event) {
		final IProject project = InWorkspaceSymbolsProvider.projectFor(event);
		if (project!=null) {
			return createFor(LiveExpression.constant(project));
		}
		return null;
	}

	private static final Duration TIMEOUT = Duration.ofSeconds(2);
	private static final int MAX_RESULTS = 200;
	
	private Supplier<List<LanguageServer>> languageServers;
	private Supplier<IProject> project;

	public InProjectSymbolsProvider(Supplier<List<LanguageServer>> languageServers, Supplier<IProject> project) {
		this.languageServers = languageServers;
		this.project = project;
	}

	@Override
	public String getName() {
		return "Symbols in Project";
	}

	@Override
	public List<Either<SymbolInformation, DocumentSymbol>> fetchFor(String query) throws Exception {
		//TODO: if we want decent support for multiple language servers...
		// consider changing SymbolsProvider api and turning the stuff in here into something producing a 
		// Flux<Collection<SymbolInformation>>
		// This will help in 
		//  - supporting cancelation
		//  - executing multiple requests to different servers in parallel.
		//  - producing results per server so don't have to wait for one slow server to see the rest.
		//However it will also add complexity to the code that consumes this and at this time we only
		// really use this with a single language server anyways.
		
		IProject project = this.project.get();
		if (project!=null) {
			String projectLocationPrefix = LSPEclipseUtils.toUri(project).toString();
			query = "locationPrefix:" + projectLocationPrefix + "?" + query;
			
			WorkspaceSymbolParams params = new WorkspaceSymbolParams(query);
			
			Flux<Either<SymbolInformation, DocumentSymbol>> symbols = Flux.fromIterable(this.languageServers.get())
					.flatMap(server -> Mono.fromFuture(server.getWorkspaceService().symbol(params))
						.timeout(TIMEOUT)
						.doOnError(e -> log(e))
						.onErrorReturn(ImmutableList.of())
						.flatMapMany(Flux::fromIterable)
						.map(symbol -> Either.forLeft(symbol))
			);
			//Consider letting the Flux go out from here instead of blocking and collecting elements.
			return symbols.take(MAX_RESULTS).collect(Collectors.toList()).block();
		}
		return ImmutableList.of();
	}
	
	@Override
	public boolean fromFile(SymbolInformation symbol) {
		return false;
	}
	
	private static void log(Throwable e) {
		GotoSymbolPlugin.getInstance().getLog().log(ExceptionUtil.status(e));
	}

}
