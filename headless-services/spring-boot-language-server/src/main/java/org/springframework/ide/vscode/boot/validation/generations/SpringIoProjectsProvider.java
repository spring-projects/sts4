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

import java.util.List;
import java.util.Map;

import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Provides Spring project definitions from a source like
 * "https://spring.io/api/projects"
 * <p/>
 * If a client is not provided, a default client will be used instead that will
 * point to "https://spring.io/api/projects"
 *
 */
public class SpringIoProjectsProvider implements SpringProjectsProvider {

	private final SpringProjectsClient client;
	private Map<String, ResolvedSpringProject> cache;

	public SpringIoProjectsProvider(SpringProjectsClient client) {
		this.client = client;
	}

	/**
	 * 
	 * @param Project slug. E.g. "spring-boot"
	 * @return
	 * @throws Exception
	 */
	@Override
	public ResolvedSpringProject getProject(String projectSlug) throws Exception {
		ResolvedSpringProject prj = cache().get(projectSlug);
		return prj;
	}

	private Map<String, ResolvedSpringProject> cache() throws Exception {
		if (cache == null) {
			SpringProjects springProjects = client.getSpringProjects();
			cache = asMap(springProjects);
		}
		return cache != null ? cache : ImmutableMap.of();
	}

	private Map<String, ResolvedSpringProject> asMap(SpringProjects springProjects) {
		Builder<String, ResolvedSpringProject> builder = ImmutableMap.builder();

		if (springProjects != null) {
			List<SpringProject> projects = springProjects.getProjects();
			if (projects != null) {
				for (SpringProject project : projects) {
					builder.put(project.getSlug(), new ResolvedSpringProject(project, client));
				}
			}
		}
		return builder.build();
	}

}
