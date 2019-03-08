/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import java.util.concurrent.TimeUnit;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsCacheKey;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * TODO: Remove this class when the 'thread leak bug' in V2 client is fixed.
 *
 * At the moment each time {@link SpringCloudFoundryClient} is create a
 * threadpool is created by the client and it is never cleaned up. The only way
 * we have to mitigate this leak is to try and create as few clients as
 * possible.
 * <p>
 * So we have a permanent cache of clients here that is reused.
 * <p>
 * When the bug is fixed then this should no longer be necessary and we can
 * removed this cache and just create the client as needed.
 *
 * @author Kris De Volder
 */
public class CloudFoundryClientCache {

	private static final boolean DEBUG = false;
	public static final long EXPIRATION = 1;

	static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private final LoadingCache<ClientParamsCacheKey, CFClientProvider> cache;

	private int clientCount = 0;

	public CloudFoundryClientCache() {
		CacheLoader<ClientParamsCacheKey, CFClientProvider> loader = new CacheLoader<ClientParamsCacheKey, CFClientProvider>() {

			@Override
			public CFClientProvider load(ClientParamsCacheKey params) throws Exception {
				clientCount++;
				debug("Creating client [" + clientCount + "]: " + params);
				return create(params.fullParams);
			}

		};
		cache = CacheBuilder.newBuilder().maximumSize(1).expireAfterAccess(EXPIRATION, TimeUnit.HOURS)
				.build(loader);

	}

	public synchronized CFClientProvider getOrCreate(CFClientParams params) throws Exception {
		return create(params);
		// Disable cache as corrupted clients may be kept due to connection or auth errors
//		return cache.get(ClientParamsCacheKey.from(params));
	}

	protected CFClientProvider create(CFClientParams params) {
		return new CFClientProvider(params);
	}
}
