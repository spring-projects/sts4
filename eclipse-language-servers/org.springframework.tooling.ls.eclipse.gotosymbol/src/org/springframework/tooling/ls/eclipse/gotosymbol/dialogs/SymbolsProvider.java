/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * An SymbolsProvider can fetch symbols from a service (typically, a language server
 * operation is used to fetch symbol data. This operation may be slow and should be
 * called only from a background job.
 */
public interface SymbolsProvider {
	String getName();
	
	List<Either<SymbolInformation, DocumentSymbol>> fetchFor(String query) throws Exception;
	/**
	 * True if the symbol information is provided from a file provider (a file is the provider of the symbols). False otherwise
	 * @param symbol
	 * @return True if the symbol information is provided from a file provider (a file is the provider of the symbols). False otherwise

	 */
	boolean fromFile(SymbolInformation symbol);
}