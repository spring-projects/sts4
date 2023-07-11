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
package org.springframework.ide.vscode.boot.index.cache;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;

import com.google.common.collect.Multimap;

/**
 * @author Martin Lippert
 */
public interface IndexCache {

	void store(IndexCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies);
	Pair<CachedSymbol[], Multimap<String, String>> retrieve(IndexCacheKey cacheKey, String[] files);

	void update(IndexCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols, Set<String> dependencies);
	void update(IndexCacheKey cacheKey, String[] files, long[] lastModified, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies);

	void remove(IndexCacheKey cacheKey);
	void removeFile(IndexCacheKey symbolCacheKey, String file);
	
	default CachedSymbol[] retrieveSymbols(IndexCacheKey cacheKey, String[] files) {
		Pair<CachedSymbol[], Multimap<String, String>> r = retrieve(cacheKey, files);
		return r!=null ? r.getLeft() : null;
	}
	
	long getModificationTimestamp(IndexCacheKey cacheKey, String docURI);

}
