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
package org.springframework.ide.vscode.commons.languageserver;

public class JavadocParams {

	private String projectUri;
	private String bindingKey;

	public JavadocParams(String projectUri, String bindingKey) {
		super();
		this.projectUri = projectUri;
		this.bindingKey = bindingKey;
	}
	public String getProjectUri() {
		return projectUri;
	}
	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}
	public String getBindingKey() {
		return bindingKey;
	}
	public void setBindingKey(String bindingKey) {
		this.bindingKey = bindingKey;
	}

}
