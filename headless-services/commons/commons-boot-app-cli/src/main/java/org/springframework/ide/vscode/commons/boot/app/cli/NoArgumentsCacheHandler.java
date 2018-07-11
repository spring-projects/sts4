/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class NoArgumentsCacheHandler implements InvocationHandler {

	private Cache<String, Object> cache;
	private Object delegate;

	public NoArgumentsCacheHandler(Object delegate, Duration cacheDuration) {
		this.delegate = delegate;
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(cacheDuration.toMillis(), TimeUnit.MILLISECONDS).build();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (args==null || args.length==0) {
			return invokeCached(proxy, method);
		}
		return method.invoke(proxy, args);
	}

	private Object invokeCached(Object proxy, Method method) throws Exception {
		return cache.get(method.getName(), () -> {
			return method.invoke(delegate, new Object[] {});
		});
	}

}
