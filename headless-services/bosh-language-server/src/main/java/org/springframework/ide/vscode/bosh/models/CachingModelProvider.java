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
package org.springframework.ide.vscode.bosh.models;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.springframework.ide.vscode.commons.yaml.schema.DynamicSchemaContext;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Wraps around a {@link DynamicModelProvider} to add a cache.
 */
public class CachingModelProvider<T> implements DynamicModelProvider<T> {

	/**
	 * Special key to use when the actual key is null (because guava cache doesn't
	 * like null keys).
	 */
	private static final Object NULL_KEY = new Object();

	private long timeout = 15;
	private TimeUnit timeoutUnit = TimeUnit.SECONDS;
	private Cache<Object, T> cache = createCache();

	private final DynamicModelProvider<T> delegate;

	public CachingModelProvider(DynamicModelProvider<T> delegate) {
		this.delegate = delegate;
	}

	/**
	 * Function used to determine the caching key, given the current {@link DynamicSchemaContext}.
	 * <p>
	 * The default keyGetter ignores the context and just returns the same object all the time. This
	 * results in a cache that only keeps a single value (since there's only a single key ever used
	 * to store / find cache entries.
	 */
	private Function<DynamicSchemaContext, Object> keyGetter = (dc) -> "WHATEVER";

	protected Cache<Object, T> createCache() {
		return CacheBuilder.newBuilder()
				.expireAfterWrite(timeout, timeoutUnit)
				.build();
	}

	public CachingModelProvider<T> setTimeout(long timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.timeoutUnit = unit;
		return this;
	}

	@Override
	public T getModel(DynamicSchemaContext dc) throws Exception {
		Object key = keyGetter.apply(dc);
		if (key==null) {
			//guava cache doesn't like null key
			key = NULL_KEY;
		}
		return cache.get(key, () -> delegate.getModel(dc));
	}

}
