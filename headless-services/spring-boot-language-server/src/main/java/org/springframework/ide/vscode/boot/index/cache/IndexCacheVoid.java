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
public class IndexCacheVoid implements IndexCache {

	@Override
	public void store(IndexCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols, Multimap<String,String> dependencies) {
	}

	@Override
	public Pair<CachedSymbol[], Multimap<String, String>> retrieve(IndexCacheKey cacheKey, String[] files) {
		return null;
	}

	@Override
	public void update(IndexCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols, Set<String> dependencies) {
	}

	@Override
	public void update(IndexCacheKey cacheKey, String[] file, long[] lastModified, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies) {
	}

	@Override
	public void remove(IndexCacheKey cacheKey) {
	}

	@Override
	public void removeFile(IndexCacheKey symbolCacheKey, String file) {
	}

	@Override
	public long getModificationTimestamp(IndexCacheKey cacheKey, String docURI) {
		return 0;
	}

}
