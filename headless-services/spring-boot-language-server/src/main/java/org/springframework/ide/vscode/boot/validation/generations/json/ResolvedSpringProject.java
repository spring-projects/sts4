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
package org.springframework.ide.vscode.boot.validation.generations.json;

import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;

import com.google.common.collect.ImmutableList;

public class ResolvedSpringProject extends SpringProject {

	private final SpringProjectsClient client;
	private Generations generations;
	private Releases releases;

	public ResolvedSpringProject(SpringProject project, SpringProjectsClient client) {
		this.client = client;
		setName(project.getName());
		setRepositoryUrl(project.getRepositoryUrl());
		setSlug(project.getSlug());
		setStatus(project.getStatus());
		set_links(project.get_links());
	}

	public List<Generation> getGenerations() throws Exception {
		// cache the generations to prevent frequent calls to the client
		if (this.generations == null) {
			Links _links = get_links();
			if (_links != null) {
				Link genLink = _links.getGenerations();
				if (genLink != null) {
					this.generations = client.getGenerations(genLink.getHref());
				}
			}
		}
		return this.generations != null ? this.generations.getGenerations() : ImmutableList.of();
	}

	public List<Release> getReleases() throws Exception {
		// cache the releases to prevent frequent calls to the client
		if (this.releases == null) {
			Links _links = get_links();
			if (_links != null) {
				Link genLink = _links.getReleases();
				if (genLink != null) {
					this.releases = client.getReleases(genLink.getHref());
				}
			}
		}
		return this.releases != null ? this.releases.getReleases() : ImmutableList.of();
	}
}
