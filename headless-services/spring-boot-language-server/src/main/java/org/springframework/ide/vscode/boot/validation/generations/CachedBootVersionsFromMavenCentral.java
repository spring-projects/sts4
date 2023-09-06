/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	private static final int ATTEMPTS_NUMBER = 5;
	private static final long RESPONSE_WAIT_TIME_MS = 1000;

	private BootVersionsFromMavenCentral bootVersionsFromMaven;
	
	public CachedBootVersionsFromMavenCentral(BootVersionsFromMavenCentral bootVersionsFromMaven) {
		this.bootVersionsFromMaven = bootVersionsFromMaven;
	}

	private final LoadingCache<String, List<Version>> cache = CacheBuilder.newBuilder()
			.expireAfterWrite(EXPIRES_AFTER)
			.build(new CacheLoader<String, List<Version>>() {

		@Override
		public List<Version> load(String key) throws Exception {
			for (int i = 0; i < ATTEMPTS_NUMBER; i++) {
				CompletableFuture<List<Version>> future = null;
				try {
					future = getFuture();
					return future.get(RESPONSE_WAIT_TIME_MS, TimeUnit.MILLISECONDS);
				} catch (ExecutionException | TimeoutException e) {
					// ignore exception - ask maven central again
					if (future != null) {
						future.cancel(true);
					}
				}
			}
			log.error("Failed to fetch versions from Maven Central after " + ATTEMPTS_NUMBER + " tries.");
			return Collections.emptyList();
		}
		
	});
	
	public synchronized List<Version> getBootVersions() {
		try {
			return cache.get(KEY);
		}
		catch (ExecutionException e) {
			log.error("Failed to load Spring Boot release information from maven central", e);
			return ImmutableList.of();
		}
	}
	
	private CompletableFuture<List<Version>> getFuture() {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return bootVersionsFromMaven.getBootVersions();
			} catch (IOException e) {
				throw new CompletionException(e);
			}
		});
	}

}
