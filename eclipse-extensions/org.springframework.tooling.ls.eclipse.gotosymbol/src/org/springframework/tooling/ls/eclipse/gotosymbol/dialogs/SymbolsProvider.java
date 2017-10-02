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

import java.util.Collection;

import org.eclipse.lsp4j.SymbolInformation;

/**
 * An SymbolsProvider can fetch symbols from a service (typically, a language server
 * operation is used to fetch symbol data. This operation may be slow and should be
 * called only from a background job.
 */
public interface SymbolsProvider {
	String getName();
	Collection<SymbolInformation> fetchFor(String query) throws Exception;
}