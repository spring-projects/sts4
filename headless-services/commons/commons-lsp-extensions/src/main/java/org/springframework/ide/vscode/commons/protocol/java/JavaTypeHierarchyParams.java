/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

public class JavaTypeHierarchyParams {
	
	private String projectUri;
	private String fqName;
	private boolean includeFocusType;
	private boolean detailed;
	
	public JavaTypeHierarchyParams(String projectUri, String fqName, boolean includeFocusType, boolean detailed) {
		super();
		this.projectUri = projectUri;
		this.fqName = fqName;
		this.setDetailed(detailed);
		this.setIncludeFocusType(includeFocusType);
	}
	
	public JavaTypeHierarchyParams(String projectUri, String fqName) {
		this(projectUri, fqName, false, false);
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getFqName() {
		return fqName;
	}

	public void setFqName(String fqName) {
		this.fqName = fqName;
	}

	public boolean isIncludeFocusType() {
		return includeFocusType;
	}

	public void setIncludeFocusType(boolean includeFocusType) {
		this.includeFocusType = includeFocusType;
	}

	public boolean isDetailed() {
		return detailed;
	}

	public void setDetailed(boolean detailed) {
		this.detailed = detailed;
	}

	/**
	 * IMPORTANT: Do not include 'detailed' flag in the {@link #hashCode()} and {@link #equals(Object)} 
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fqName == null) ? 0 : fqName.hashCode());
		result = prime * result + (includeFocusType ? 1231 : 1237);
		result = prime * result + ((projectUri == null) ? 0 : projectUri.hashCode());
		return result;
	}

	/**
	 * IMPORTANT: Do not include 'detailed' flag in the {@link #hashCode()} and {@link #equals(Object)} 
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaTypeHierarchyParams other = (JavaTypeHierarchyParams) obj;
		if (fqName == null) {
			if (other.fqName != null)
				return false;
		} else if (!fqName.equals(other.fqName))
			return false;
		if (includeFocusType != other.includeFocusType)
			return false;
		if (projectUri == null) {
			if (other.projectUri != null)
				return false;
		} else if (!projectUri.equals(other.projectUri))
			return false;
		return true;
	}

}
