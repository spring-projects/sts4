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

import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.boot.validation.generations.json.Link;
import org.springframework.ide.vscode.boot.validation.generations.json.Links;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProject;
import org.springframework.ide.vscode.boot.validation.generations.json.SpringProjects;

public class SpringProjectIndex {

	private final SpringProjectsClient client;

	public SpringProjectIndex(SpringProjectsClient client) {
		this.client = client;
	}

	public SpringProjects getProjects() throws Exception {
		return client.getSpringProjects();
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

	/**
	 * 
	 * @param projectSlug slug used by Spring projects metadata, for example
	 *                    "spring-boot" for Spring Boot projects
	 * @return
	 * @throws Exception
	 */
	public SpringProject getProject(String projectSlug) throws Exception {
		SpringProjects springProjects = getProjects();
		List<SpringProject> projects = springProjects.getProjects();
		SpringProject prj = null;
		for (SpringProject project : projects) {
			if (project.getSlug().equals(projectSlug)) {
				prj = project;
				break;
			}
		}
		return prj;
	}

}
