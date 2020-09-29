/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.util.List;

/**
 * @author Kris De Volder
 */
public class Bom {

	private String id;
	private IMavenCoordinates coords;
	private List<Repo> repos;

	public Bom(String id, IMavenCoordinates coords, List<Repo> repos) {
		this.id = id;
		this.coords = coords;
		this.repos = repos;
	}

	public String getId() {
		return id;
	}
	public IMavenCoordinates getCoords() {
		return coords;
	}
	public List<Repo> getRepos() {
		return repos;
	}

	public String getGroupId() {
		return getCoords().getGroupId();
	}
	public String getArtifactId() {
		return getCoords().getArtifactId();
	}
	public String getVersion() {
		return getCoords().getVersion();
	}
	public String getClassifier() {
		return getCoords().getClassifier();
	}
}
