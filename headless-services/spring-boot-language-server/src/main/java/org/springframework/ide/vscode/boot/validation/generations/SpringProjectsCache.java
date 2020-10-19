/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.Link;
import org.springframework.ide.vscode.boot.validation.generations.json.Links;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.AsyncRunner;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public class SpringProjectsCache {

	private static final long TIMEOUT_SECS = 30;
	private final SpringProjectsClient client;
	private final SimpleLanguageServer server;
	private Map<String, SpringProject> cache;

	public SpringProjectsCache(SpringProjectsClient client, SimpleLanguageServer server) {
		this.client = client;
		this.server = server;
	}

	/**
	 * 
	 * @param Project slug. E.g. "spring-boot"
	 * @return
	 * @throws Exception
	 */
	public SpringProject getProject(String slug) throws Exception {
		return cache().get(slug);
	}
	
	public Generations getGenerations(SpringProject project) throws Exception {
		if (project != null) {
			Links _links = project.get_links();
			if (_links != null) {
				Link genLink = _links.getGenerations();
				if (genLink != null) {
					return client.getGenerations(genLink.getHref());
				}
			}
		}
		return null;
	}

	private Map<String, SpringProject> cache() throws Exception {
		if (cache == null) {
			loadCache();
		}
		return cache != null ? cache : ImmutableMap.of();
	}

	private void loadCache() throws Exception {
		AsyncRunner async = this.server.getAsync();
		if (async != null) {
			async.invoke(Duration.ofSeconds(TIMEOUT_SECS), () -> {
				SpringProjects springProjects = client.getSpringProjects();
				return asMap(springProjects);
			}).thenAccept((map) -> cache = map).get();
		}
	}

	private Map<String, SpringProject> asMap(SpringProjects springProjects) {
		Builder<String, SpringProject> builder = ImmutableMap.builder();

		if (springProjects != null) {
			List<SpringProject> projects = springProjects.getProjects();
			if (projects != null) {
				for (SpringProject project : projects) {
					builder.put(project.getSlug(), project);
				}
			}
		}
		return builder.build();
	}
}
