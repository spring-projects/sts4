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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
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
	private final LoadingCache<String, List<CFBuildpack>> buildpacksCache;
	private final LoadingCache<String, List<CFServiceInstance>> servicesCache;

	public CFTarget(String targetName, CFClientParams params, ClientRequests requests) {
		this.params = params;
		this.requests = requests;
		this.targetName = targetName;
		CacheLoader<String, List<CFServiceInstance>> servicesLoader = new CacheLoader<String, List<CFServiceInstance>>() {
			
			@Override
			public List<CFServiceInstance> load(String key) throws Exception {
				/*
				 * Ignore the key. Not used.
				 */
				return requests.getServices();
			}
		};
		this.servicesCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.EXPIRATION_20_SECS, TimeUnit.SECONDS).build(servicesLoader);

		CacheLoader<String, List<CFBuildpack>> buildpacksLoader = new CacheLoader<String, List<CFBuildpack>>() {

			@Override
			public List<CFBuildpack> load(String key) throws Exception {
				/*
				 * Ignore the key. Not used.
				 */
				return requests.getBuildpacks();
			}
		};
		this.buildpacksCache = CacheBuilder.newBuilder()
				.expireAfterAccess(CFTargetCache.EXPIRATION_1_HOUR, TimeUnit.HOURS).build(buildpacksLoader);
	}

	public CFClientParams getParams() {
		return params;
	}

	public List<CFBuildpack> getBuildpacks() throws ExecutionException {
		return this.buildpacksCache.get(getName());
	}

	public List<CFServiceInstance> getServices() throws ExecutionException {
		return this.servicesCache.get(getName());
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
}
