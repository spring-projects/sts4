/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.List;

/**
 * @author Martin Lippert
 */
public interface SymbolCache {

	void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols);
	CachedSymbol[] retrieve(SymbolCacheKey cacheKey, String[] files);

	void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols);

	void remove(SymbolCacheKey cacheKey);
	void removeFile(SymbolCacheKey symbolCacheKey, String file);

}
