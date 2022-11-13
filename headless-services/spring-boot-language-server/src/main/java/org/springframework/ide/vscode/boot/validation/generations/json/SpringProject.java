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

public class SpringProject extends JsonHalLinks {
	
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
	
	protected void setName(String name) {
		this.name = name;
	}

	protected void setSlug(String slug) {
		this.slug = slug;
	}

	protected void setStatus(String status) {
		this.status = status;
	}

	protected void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}
}
