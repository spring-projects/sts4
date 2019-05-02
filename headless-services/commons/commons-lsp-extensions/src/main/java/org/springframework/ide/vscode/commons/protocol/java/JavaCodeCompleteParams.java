/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

/**
 * @author Martin Lippert
 */
public class JavaCodeCompleteParams {

	private String projectUri;
	private String prefix;
	private boolean includeTypes;
	private boolean includePackages;
	
	public JavaCodeCompleteParams(String projectUri, String prefix, boolean includeTypes, boolean includePackages) {
		super();
		this.projectUri = projectUri;
		this.prefix = prefix;
		this.includeTypes = includeTypes;
		this.includePackages = includePackages;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isIncludeTypes() {
		return includeTypes;
	}

	public void setIncludeTypes(boolean includeTypes) {
		this.includeTypes = includeTypes;
	}

	public boolean isIncludePackages() {
		return includePackages;
	}

	public void setIncludePackages(boolean includePackages) {
		this.includePackages = includePackages;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (includePackages ? 1231 : 1237);
		result = prime * result + (includeTypes ? 1231 : 1237);
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((projectUri == null) ? 0 : projectUri.hashCode());
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
		JavaCodeCompleteParams other = (JavaCodeCompleteParams) obj;
		if (includePackages != other.includePackages)
			return false;
		if (includeTypes != other.includeTypes)
			return false;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		if (projectUri == null) {
			if (other.projectUri != null)
				return false;
		} else if (!projectUri.equals(other.projectUri))
			return false;
		return true;
	}

}
