/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

public class CFTargetCache {

	private Logger logger = LoggerFactory.getLogger(CFTargetCache.class);
	private final CloudFoundryClientFactory clientFactory;
	private final ClientTimeouts timeouts;
	private LoadingCache<ClientParamsCacheKey, CFTarget> cache;
	private List<ClientParamsProvider> _providers;

	public static final Duration SERVICES_EXPIRATION = Duration.ofSeconds(10);
	public static final Duration TARGET_EXPIRATION = Duration.ofHours(1);
	public static final Duration ERROR_EXPIRATION = Duration.ofSeconds(10);

	public CFTargetCache(List<ClientParamsProvider> providers, CloudFoundryClientFactory clientFactory,
			ClientTimeouts timeouts) {
		this.clientFactory = clientFactory;
		this.timeouts = timeouts;
		this._providers = providers;
		initCache();
	}

	
	private void initCache() {
		CacheLoader<ClientParamsCacheKey, CFTarget> loader = new CacheLoader<ClientParamsCacheKey, CFTarget>() {

			@Override
			public CFTarget load(ClientParamsCacheKey key) throws Exception {
				return create(key.fullParams, key.getProvider());
			}

		};
		cache = CacheBuilder.newBuilder()./*maximumSize(1).*/expireAfterAccess(TARGET_EXPIRATION.toMillis(), TimeUnit.MILLISECONDS)
				.build(loader);
	}
	
	/**
	 * 
	 * @param providers list of providers that will be called in order.
	 */
	public synchronized void setProviders(ClientParamsProvider... providers) {
		this._providers = providers != null ? Arrays.asList(providers) : ImmutableList.of();
	}

	/**
	 * @return non-null list of targets, or throws exception if no targets found
	 * @throws NoTargetsException
	 *             if no targets found
	 * @throws Exception
	 *             for any other error encountered
	 */
	public synchronized List<CFTarget> getOrCreate() throws NoTargetsException, Exception {
		// Obtain an uptodate list of params from the providers and refresh the list of targets.
		List<CFTarget> targets = new ArrayList<>();

		Exception lastError = null;
		for (ClientParamsProvider provider : this._providers) {

			// IMPORTANT: do not let errors stop iterating through all the providers.
			// If one provider cannot provide targets, try the next one.
			try {
				Collection<CFClientParams> providerParams = provider.getParams();
				getTargets(targets, provider, providerParams);
			} catch (Exception e) {
				lastError = e;
			}
		}

		if (targets.isEmpty() && lastError != null) {
			throw lastError;
		}
		return targets;
	}


	private void getTargets(List<CFTarget> targets, ClientParamsProvider provider,
			Collection<CFClientParams> providerParams) throws ExecutionException {
		for (CFClientParams params : providerParams) {
			ClientParamsCacheKey key = ClientParamsCacheKey.from(params, provider);
			CFTarget target = cache.get(key);
			if (target != null) {
				// If any CF errors occurred in the target, refresh once
				if (target.hasExpiredConnectionError()) {
					cache.refresh(key);
					target = cache.get(key);
				}
				targets.add(target);
			}
		}
	}
	
	public synchronized List<ClientParamsProvider> getParamsProviders() {
		return this._providers;
	}


	protected CFTarget create(CFClientParams params, ClientParamsProvider provider) throws Exception {
		TargetDiagnosticMessages messages = provider.getMessages();
		CFCallableContext context = createCallingContext(provider);

		CFTarget target = new CFTarget(getTargetName(params), params, clientFactory.getClient(params, timeouts),
				context);
		if (messages != null && StringUtil.hasText(messages.getTargetSource())) {
			logger.info("Created CF target for [{}/{}], from {}", params.getOrgName(), params.getSpaceName(),
					messages.getTargetSource());
		} else {
			logger.info("Created CF target for [{}/{}]", params.getOrgName(), params.getSpaceName());
		}
		return target;
	}

	private CFCallableContext createCallingContext(ClientParamsProvider provider) {
		return new CFCallableContext(provider.getMessages());
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
