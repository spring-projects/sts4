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
package org.springframework.ide.vscode.boot.validation.generations.json;

import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;

public class SpringProject extends JsonHalLinks {
	
	private Generations generations;


	private String name;
	private String slug;
	private String status;
	private String repositoryUrl;
	
	public String getName() {
		return name;
	}

	public String getSlug() {
		return slug;
	}


	public String getStatus() {
		return status;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}
	
	public Generations getGenerations(SpringProjectsClient client) throws Exception {
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
		return this.generations;
	}
}
