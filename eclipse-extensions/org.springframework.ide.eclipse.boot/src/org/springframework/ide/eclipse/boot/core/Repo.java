/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import org.springframework.ide.eclipse.boot.core.initializr.InitializrDependencySpec.RepoInfo;

/**
 * @author Kris De Volder
 */
public class Repo {

	private String id;
	private String name;
	private String url;
	private Boolean snapshotEnabled;

	private Repo(String id, String name, String url, Boolean snapshotEnabled) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.snapshotEnabled = snapshotEnabled;
	}
	public Repo(String id, RepoInfo repo) {
		this(id, repo.getName(), repo.getUrl(), repo.getSnapshotEnabled());
	}
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getUrl() {
		return url;
	}
	public Boolean getSnapshotEnabled() {
		return snapshotEnabled;
	}
}
