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

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFDomain;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFStack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 *
 * Wrapper around a {@link ClientRequests} that may contain cached information
 * like buildpacks
 *
 */
public class CFTarget {

	private final CFClientParams params;
	private final ClientRequests requests;
	private final String targetName;

	/*
	 * Cached information
	 */
	private LoadingCache<String, List<CFBuildpack>> buildpacksCache;
	private LoadingCache<String, List<CFServiceInstance>> servicesCache;
	private LoadingCache<String, List<CFDomain>> domainCache;
	private LoadingCache<String, List<CFStack>> stacksCache;
	private CFCallableContext callableContext;
	
	public CFTarget(String targetName, CFClientParams params, ClientRequests requests,
			CFCallableContext callableContext) {
		this.params = params;
		this.requests = requests;
		this.targetName = targetName;
		this.callableContext = callableContext;
		initCache(requests);
	}

	private void initCache(ClientRequests requests) {
		CacheLoader<String, List<CFStack>> stacksLoader = new CacheLoader<String, List<CFStack>>() {

			@Override
			public List<CFStack> load(String key) throws Exception {
				/* Cache of services does not use keys, as the whole cache
				 * gets wiped clean on any new call to CF.
				 */
				return runAndCheckForFailure(() -> requests.getStacks());
			}
		};
		this.stacksCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.TARGET_EXPIRATION.toMillis(), TimeUnit.MILLISECONDS).build(stacksLoader);


		CacheLoader<String, List<CFServiceInstance>> servicesLoader = new CacheLoader<String, List<CFServiceInstance>>() {

			@Override
			public List<CFServiceInstance> load(String key) throws Exception {
				/* Cache of services does not use keys, as the whole cache
				 * gets wiped clean on any new call to CF.
				 */
				return runAndCheckForFailure(() -> requests.getServices());
			}
		};
		this.servicesCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.SERVICES_EXPIRATION.toMillis(), TimeUnit.MILLISECONDS).build(servicesLoader);

		CacheLoader<String, List<CFBuildpack>> buildpacksLoader = new CacheLoader<String, List<CFBuildpack>>() {

			@Override
			public List<CFBuildpack> load(String key) throws Exception {
				/* Cache does not use keys, as the whole cache
				 * gets wiped clean on any new call to CF.
				 */
				return runAndCheckForFailure(() -> requests.getBuildpacks());
			}
		};
		this.buildpacksCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.TARGET_EXPIRATION.toMillis(), TimeUnit.MILLISECONDS).build(buildpacksLoader);

		CacheLoader<String, List<CFDomain>> domainLoader = new CacheLoader<String, List<CFDomain>>() {

			@Override
			public List<CFDomain> load(String key) throws Exception {
				return runAndCheckForFailure(() -> requests.getDomains());
			}

		};
		this.domainCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.TARGET_EXPIRATION.toMillis(), TimeUnit.MILLISECONDS).build(domainLoader);
	}

	protected <T> T runAndCheckForFailure(Callable<T> callable) throws Exception {
		return callableContext.run(callable);
	}

	public boolean hasExpiredConnectionError() {
		return callableContext.hasExpiredConnectionError();
	}

	public CFClientParams getParams() {
		return params;
	}

	public List<CFStack> getStacks() throws Exception {
		// Use the target name as the "key" , since Guava cache doesn't allow null keys
		// However, the key is not really used when fetching buildpacks, as we are not caching
		// buildpacks per target here. This class only represents ONE target, so it will only
		// ever have one key
		String key = getName();
		return this.stacksCache.get(key);
	}

	public List<CFBuildpack> getBuildpacks() throws Exception {
		// Use the target name as the "key" , since Guava cache doesn't allow null keys
		// However, the key is not really used when fetching buildpacks, as we are not caching
		// buildpacks per target here. This class only represents ONE target, so it will only
		// ever have one key
		String key = getName();
		return this.buildpacksCache.get(key);
	}

	public List<CFServiceInstance> getServices() throws Exception {
		/* services don't use keys, as they get wiped clean on each refresh
		 * . That said, the cache doesn't allow a null key, so use the target name as the "key"
		 *
		 */
		String key = getName();
		return this.servicesCache.get(key);
	}

	public List<CFDomain> getDomains() throws Exception {
		String key = getName();
		return this.domainCache.get(key);
	}

	public ClientRequests getClientRequests() {
		return requests;
	}

	public String getName() {
		return this.targetName;
	}

	@Override
	public String toString() {
		return "CFClientTarget [params=" + params + ", targetName=" + targetName + "]";
	}

	public String getLabel() {
//		%o : %s - [%a]
		return params.getOrgName() + " : " + params.getSpaceName() + " ["+params.getApiUrl()+"]";
	}

}
