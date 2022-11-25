/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.Version;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;

public class CachedBootVersionsFromMavenCentral {
	
	private static final Logger log = LoggerFactory.getLogger(CachedBootVersionsFromMavenCentral.class);
	
	private static final String KEY = "cacheKey";
	private static final Duration EXPIRES_AFTER = Duration.ofMinutes(60);
	
	private static final LoadingCache<String, List<Version>> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(EXPIRES_AFTER)
			.build(new CacheLoader<String, List<Version>>() {

		@Override
		public List<Version> load(String key) {
			try {
				return BootVersionsFromMavenCentral.getBootVersions();
			}
			catch (Exception e) {
				return ImmutableList.of();
			}
		}
		
	});
	
	public static List<Version> getBootVersions() {
		try {
			return cache.get(KEY);
		}
		catch (Exception e) {
			log.error("failed to load Spring Boot release information from maven central", e);
			return ImmutableList.of();
		}
	}

}
