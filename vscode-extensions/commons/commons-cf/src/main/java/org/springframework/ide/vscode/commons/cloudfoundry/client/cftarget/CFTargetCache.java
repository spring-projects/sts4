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
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.util.Assert;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class CFTargetCache {

	private final ClientParamsProvider paramsProvider;
	private final CloudFoundryClientFactory clientFactory;
	private final ClientTimeouts timeouts;
	private final LoadingCache<CFClientParams, CFTarget> cache;

	public static final long EXPIRATION_20_SECS = 20;
	public static final long EXPIRATION_1_HOUR = 1;

	public CFTargetCache(ClientParamsProvider paramsProvider, CloudFoundryClientFactory clientFactory,
			ClientTimeouts timeouts) {
		Assert.isLegal(paramsProvider != null,
				"A Cloud Foundry client parameters provider must be set when creating a target cache.");
		this.paramsProvider = paramsProvider;
		this.clientFactory = clientFactory;
		this.timeouts = timeouts;
		CacheLoader<CFClientParams, CFTarget> loader = new CacheLoader<CFClientParams, CFTarget>() {

			@Override
			public CFTarget load(CFClientParams params) throws Exception {
				return create(params);
			}

		};
		cache = CacheBuilder.newBuilder().expireAfterAccess(EXPIRATION_1_HOUR, TimeUnit.HOURS).build(loader);
	}

	/**
	 * @return non-null list of targets, or throws exception if no targets found
	 * @throws Exception if no targets found, or error in resolving targets
	 */
	public synchronized List<CFTarget> getOrCreate() throws Exception {

		List<CFClientParams> allParams = paramsProvider.getParams();
		List<CFTarget> targets = new ArrayList<>();
		if (allParams != null) {
			for (CFClientParams params : allParams) {
				CFTarget target = cache.get(params);
				if (target != null) {
					targets.add(target);
				}
			}
		}

		if (targets.isEmpty()) {
			throw new ExecutionException(new Error(paramsProvider.noParamsAvailableMessage()));
		}

		return targets;
	}

	protected CFTarget create(CFClientParams params) throws Exception {
		return new CFTarget(getTargetName(params), params, clientFactory.getClient(params, timeouts));
	}

	protected static String getTargetName(CFClientParams params) {
		return labelFromCfApi(params.getApiUrl());
	}

	protected static String labelFromCfApi(String cfApiUrl) {
		if (cfApiUrl.startsWith("https://")) {
			return cfApiUrl.substring("https://".length());
		} else if (cfApiUrl.startsWith("http://")) {
			return cfApiUrl.substring("http://".length());
		} else {
			return cfApiUrl;
		}
	}
}
