/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.internal.plugins;

/**
 * Very basic metadata for a plugin that only contains property information.
 * 
 * @author Nieraj Singh
 */
public class BasePluginData {

	private String name;
	private String version;
	private String runtimeVersion;
	private String author;
	private String title;
	private String description;
	private String documentation;

	public void setName(String name) {
		this.name = name;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	/**
	 * Must never be null. Use empty string if null
	 * @param version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	public void setRuntimeVersion(String runtimeVersion) {
		this.runtimeVersion = runtimeVersion;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Must never be null.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Never null, but may be empty.
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	public String getDocumentation() {
		return documentation;
	}

	public String getRuntimeVersion() {
		return runtimeVersion;
	}

	public String getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
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
		BasePluginData other = (BasePluginData) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	
}
