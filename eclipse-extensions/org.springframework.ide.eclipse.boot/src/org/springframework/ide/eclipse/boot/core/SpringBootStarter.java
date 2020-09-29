/*******************************************************************************
 * Copyright (c) 2012-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

/**
 * A 'SpringBootStarter is maven style dependency that can be added to
 * a boot enabled project.
 *
 * @author Kris De Volder
 */
public class SpringBootStarter {

	private String id; //id used by initalizr service
	private IMavenCoordinates dep;
	private String scope;
	private Bom bom;
	private Repo repo;

	public SpringBootStarter(String id, IMavenCoordinates dep, String scope, Bom bom, Repo repo) {
		this.id = id;
		this.dep = dep;
		this.scope = scope;
		this.bom = bom;
		this.repo = repo;
	}
	public String getId() {
		return id;
	}
	public String getArtifactId() {
		return dep.getArtifactId();
	}
	public String getGroupId() {
		return dep.getGroupId();
	}
	public String getVersion() {
		return dep.getVersion();
	}
	public IMavenCoordinates getDependency() {
		return dep;
	}
	public String getScope() {
		return scope;
	}
	public Bom getBom() {
		return bom;
	}
	public Repo getRepo() {
		return repo;
	}

	/**
	 * GroupId + ArtifactId, can be used as a key in a map of SpringBootStarter objects.
	 * Typically the gid + aid will identify the starter. The version is 'fixed' within
	 * a project so it isn't part of the 'id'.
	 */
	public MavenId getMavenId() {
		return new MavenId(dep.getGroupId(), dep.getArtifactId());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SpringBootStarter other = (SpringBootStarter) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "SpringBootStarter("+id+")";
	}
}
