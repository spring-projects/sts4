/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

public class JavaDataParams {

	private String projectUri;
	private String bindingKey;
	private Boolean lookInOtherProjects;

	public JavaDataParams(String projectUri, String bindingKey, boolean lookInOtherProjects) {
		super();
		this.projectUri = projectUri;
		this.bindingKey = bindingKey;
		this.lookInOtherProjects = lookInOtherProjects;
	}

	public String getBindingKey() {
		return bindingKey;
	}

	public void setBindingKey(String bindingKey) {
		this.bindingKey = bindingKey;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String project) {
		this.projectUri = project;
	}

	public Boolean getLookInOtherProjects() {
		return lookInOtherProjects;
	}

	@Override
	public String toString() {
		return "JavaDataParams [projectUri=" + projectUri + ", bindingKey=" + bindingKey + ", lookInOtherProjects="
				+ lookInOtherProjects + "]";
	}

	public void setLookInOtherProjects(Boolean lookInOtherProjects) {
		this.lookInOtherProjects = lookInOtherProjects;
	}
	
	public static boolean isLookInOtherProjects(JavaDataParams params) {
		return isLookInOtherProjects(params.projectUri, params.getLookInOtherProjects());
	}
	
	public static boolean isLookInOtherProjects(String projectUri, Boolean lookInOtherProjects) {
		return projectUri == null ? true : lookInOtherProjects == null ? false : lookInOtherProjects.booleanValue();
	}

}
