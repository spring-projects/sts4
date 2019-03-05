/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.List;

/**
 * @author Martin Lippert
 */
public class SymbolCacheVoid implements SymbolCache {

	@Override
	public void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols) {
	}

	@Override
	public CachedSymbol[] retrieve(SymbolCacheKey cacheKey, String[] files) {
		return null;
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols) {
	}

	@Override
	public void remove(SymbolCacheKey cacheKey) {
	}

	@Override
	public void removeFile(SymbolCacheKey symbolCacheKey, String file) {
	}

}
