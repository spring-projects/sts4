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
public interface SymbolCache {

	void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies);
	Pair<CachedSymbol[], Multimap<String, String>> retrieve(SymbolCacheKey cacheKey, String[] files);

	void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols, Set<String> dependencies);
	void update(SymbolCacheKey cacheKey, String[] files, long[] lastModified, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies);

	void remove(SymbolCacheKey cacheKey);
	void removeFile(SymbolCacheKey symbolCacheKey, String file);
	
	default CachedSymbol[] retrieveSymbols(SymbolCacheKey cacheKey, String[] files) {
		Pair<CachedSymbol[], Multimap<String, String>> r = retrieve(cacheKey, files);
		return r!=null ? r.getLeft() : null;
	}
	
	long getModificationTimestamp(SymbolCacheKey cacheKey, String docURI);

}
