/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
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
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Multimap;

/**
 * @author Martin Lippert
 */
public class SymbolCacheVoid implements SymbolCache {

	@Override
	public void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols, Multimap<String,String> dependencies) {
	}

	@Override
	public Pair<CachedSymbol[], Multimap<String, String>> retrieve(SymbolCacheKey cacheKey, String[] files) {
		return null;
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols, Set<String> dependencies) {
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String[] file, long[] lastModified, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies) {
	}

	@Override
	public void remove(SymbolCacheKey cacheKey) {
	}

	@Override
	public void removeFile(SymbolCacheKey symbolCacheKey, String file) {
	}

	@Override
	public long getModificationTimestamp(SymbolCacheKey cacheKey, String docURI) {
		return 0;
	}

}
