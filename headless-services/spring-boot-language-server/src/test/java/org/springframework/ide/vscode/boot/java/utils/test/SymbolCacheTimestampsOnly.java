/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheKey;

import com.google.common.collect.Multimap;

/**
 * @author Martin Lippert
 */
public class SymbolCacheTimestampsOnly implements SymbolCache {
	
	private Map<SymbolCacheKey, Map<String, Long>> timestampCache; 
	
	public SymbolCacheTimestampsOnly() {
		this.timestampCache = new HashMap<>();
	}

	@Override
	public void store(SymbolCacheKey cacheKey, String[] files, List<CachedSymbol> generatedSymbols, Multimap<String,String> dependencies) {
		SortedMap<String, Long> timestampedFiles = new TreeMap<>();

		timestampedFiles = Arrays.stream(files)
				.filter(file -> new File(file).exists())
				.collect(Collectors.toMap(file -> file, file -> {
					try {
						return Files.getLastModifiedTime(new File(file).toPath()).toMillis();
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}, (v1,v2) -> { throw new RuntimeException(String.format("Duplicate key for values %s and %s", v1, v2));}, TreeMap::new));

		timestampCache.put(cacheKey, timestampedFiles);
	}

	@Override
	public Pair<CachedSymbol[], Multimap<String, String>> retrieve(SymbolCacheKey cacheKey, String[] files) {
		return null;
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String file, long lastModified, List<CachedSymbol> generatedSymbols, Set<String> dependencies) {
		Map<String, Long> timestampMap = timestampCache.get(cacheKey);
		timestampMap.put(file, lastModified);
	}

	@Override
	public void update(SymbolCacheKey cacheKey, String[] files, long[] lastModified, List<CachedSymbol> generatedSymbols, Multimap<String, String> dependencies) {
		Map<String, Long> timestampMap = timestampCache.get(cacheKey);

		for (int i = 0; i < files.length; i++) {
			timestampMap.put(files[i], lastModified[i]);
		}
	}

	@Override
	public void remove(SymbolCacheKey cacheKey) {
	}

	@Override
	public void removeFile(SymbolCacheKey symbolCacheKey, String file) {
	}

	@Override
	public long getModificationTimestamp(SymbolCacheKey cacheKey, String file) {
		Map<String, Long> timestampMap = timestampCache.get(cacheKey);
		if (timestampMap != null) {
			Long timestamp = timestampMap.get(file);
			if (timestamp != null) {
				return timestamp;
			}
		}
		
		return 0;
	}

}
