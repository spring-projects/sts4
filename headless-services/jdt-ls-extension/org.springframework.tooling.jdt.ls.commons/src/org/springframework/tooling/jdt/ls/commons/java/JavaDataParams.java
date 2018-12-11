/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

public class JavaDataParams {

	private String projectUri;
	private String bindingKey;

	public JavaDataParams(String projectUri, String bindingKey) {
		super();
		this.projectUri = projectUri;
		this.bindingKey = bindingKey;
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

	@Override
	public String toString() {
		return "JavaDataParams [projectUri=" + projectUri + ", bindingKey=" + bindingKey + "]";
	}

}
