/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbol;

/**
 * An SymbolsProvider can fetch symbols from a service (typically, a language server
 * operation is used to fetch symbol data. This operation may be slow and should be
 * called only from a background job.
 */
public interface SymbolsProvider {
	String getName();
	
	List<SymbolContainer> fetchFor(String query) throws Exception;
	/**
	 * True if the symbol information is provided from a file provider (a file is the provider of the symbols). False otherwise
	 * @param symbol
	 * @return True if the symbol information is provided from a file provider (a file is the provider of the symbols). False otherwise

	 */
	boolean fromFile(SymbolContainer symbol);

	/**
	 * @return URI of the document that is the source of the symbols of a file provider, <code>null</code> for any other provider.
	 * Document symbols do not carry a location of their own, so this is needed to open the document for such a symbol.
	 */
	default URI getDocumentUri() {
		return null;
	}

	// helper methods for symbol providers to convert lists
	static List<SymbolContainer> toSymbolContainerFromSymbolInformation(List<? extends SymbolInformation> symbols) {
		return symbols.stream().map(sym -> SymbolContainer.fromSymbolInformation(sym)).collect(Collectors.toList());
	}

	static List<SymbolContainer> toSymbolContainerFromWorkspaceSymbols(List<? extends WorkspaceSymbol> symbols) {
		return symbols.stream().map(sym -> SymbolContainer.fromWorkspaceSymbol(sym)).collect(Collectors.toList());
	}

}