/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.concourse.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Simple cache implementation that falls back on 'stale' cache entry if
 * a new entry can not be computed. The api is loosely modeled after 
 * guava's Cache interface (but only the subset we use is implemented to reduce the
 * complexity of its implementation).
 */
public class StaleFallbackCache<K, V>{

	Map<K, V> staleEntries = new HashMap<>();
	Cache<K, CompletableFuture<V>> validEntries = CacheBuilder.newBuilder().build();


	public synchronized V get(K key, boolean allowStaleEntries, Callable<? extends V> valueLoader) throws Exception {
		CompletableFuture<V> valid = validEntries.get(key, () -> load(valueLoader));
		if (!allowStaleEntries) {
			return future_get(valid);
		} else {
			if (valid.isCompletedExceptionally()) {
				V staleValue = staleEntries.get(key);
				if (staleValue!=null) {
					return staleValue;
				}
			}
			return future_get(valid);
		}
	}
	
	public synchronized void invalidate(K key) {
		CompletableFuture<V> staleEntry = validEntries.getIfPresent(key);
		if (staleEntry!=null) {
			validEntries.invalidate(key);
			try {
				staleEntries.put(key, future_get(staleEntry));
			} catch (Exception e) {
				//ignore. Don't overwrite stale entry if current entry represents an error.
				// We only keep 'good quality' stale entries not failed attempts to compute a value.
				// as it is kind of the point to fall back on a 'good' old entry when the current
				// entry is unavailable because of a problem (e.g. problems parsing the AST).
			}
		}
	}



	private V future_get(CompletableFuture<V> f) throws Exception {
		try {
			return f.get();
		} catch (InterruptedException e) {
			throw e;
		} catch (ExecutionException e) {
			throw ExceptionUtil.exception(e.getCause());
		}
	}

	private CompletableFuture<V> load(Callable<? extends V> valueLoader) {
		CompletableFuture<V> future = new CompletableFuture<V>();
		try {
			V value = valueLoader.call();
			Assert.isNotNull(value);
			future.complete(value);
		} catch (Throwable e) {
			future.completeExceptionally(e);
		}
		return future;
	}

}
