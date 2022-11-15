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
package org.springframework.ide.vscode.commons.protocol.java;

public class ProjectBuild {
	
	public static final String MAVEN_PROJECT_TYPE = "maven";
	public static final String GRADLE_PROJECT_TYPE = "gradle";
	
	private String type;
	
	private String buildFile;
	
	public ProjectBuild(String type, String buildFile) {
		this.type = type;
		this.buildFile = buildFile;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBuildFile() {
		return buildFile;
	}

	public void setBuildFile(String buildFile) {
		this.buildFile = buildFile;
	}

	@Override
	public String toString() {
		return "ProjectBuild [type=" + type + ", buildFile=" + buildFile + "]";
	}

}
