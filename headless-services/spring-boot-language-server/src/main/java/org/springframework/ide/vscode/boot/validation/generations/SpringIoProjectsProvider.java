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

import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.Link;
import org.springframework.ide.vscode.boot.validation.generations.json.Links;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Provides Spring project definitions from a source like "https://spring.io/api/projects"
 * <p/>
 * If a client is not provided, a default client
 * will be used instead that will point to "https://spring.io/api/projects"
 *
 */
public class SpringIoProjectsProvider implements SpringProjectsProvider {

	private final SpringProjectsClient client;
	private Map<String, SpringIoProject> cache;

	public SpringIoProjectsProvider(SpringProjectsClient client) {
		this.client = client;
	}
	
	public SpringIoProjectsProvider() {
		this(getDefaultClient());
	}

	/**
	 * 
	 * @param Project slug. E.g. "spring-boot"
	 * @return
	 * @throws Exception
	 */
	@Override
	public SpringProject getProject(String projectSlug) throws Exception {
		SpringIoProject prj = cache().get(projectSlug);
		return prj != null ? prj.getProject() : null;
	}
	
	@Override
	public Generations getGenerations(String projectSlug) throws Exception {
		SpringIoProject project = cache().get(projectSlug);
		if (project != null) {
			return project.getGenerations();
		}
		return null;
	}

	private Map<String, SpringIoProject> cache() throws Exception {
		if (cache == null) {
			SpringProjects springProjects = client.getSpringProjects();
			cache = asMap(springProjects);
		}
		return cache != null ? cache : ImmutableMap.of();
	}

	private Map<String, SpringIoProject> asMap(SpringProjects springProjects) {
		Builder<String, SpringIoProject> builder = ImmutableMap.builder();

		if (springProjects != null) {
			List<SpringProject> projects = springProjects.getProjects();
			if (projects != null) {
				for (SpringProject project : projects) {
					builder.put(project.getSlug(), new SpringIoProject(project, this.client));
				}
			}
		}
		return builder.build();
	}
	
	private static SpringProjectsClient getDefaultClient() {
		String url = "https://spring.io/api/projects";
		return  new SpringProjectsClient(url);
	}
	
	/**
	 * Wrapper around the JSON SpringProject that also contains its generations
	 *
	 */
	private final static class SpringIoProject {

		private final SpringProject project;
		private final SpringProjectsClient client;
		private Generations generations;
		

		public SpringIoProject(SpringProject project, SpringProjectsClient  client) {
			this.project = project;
			this.client = client;
		}

		public SpringProject getProject() {
			return this.project;
		}

		public Generations getGenerations() throws Exception {
			// cache the generations to prevent frequent calls to the client
			if (this.generations == null) {
				Links _links = project.get_links();
				if (_links != null) {
					Link genLink = _links.getGenerations();
					if (genLink != null) {
						this.generations = client.getGenerations(genLink.getHref());
					}
				}
			}
			return this.generations;
		}
	}
}
