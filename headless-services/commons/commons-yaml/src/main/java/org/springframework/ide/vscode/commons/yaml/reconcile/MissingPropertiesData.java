/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.reconcile;

import java.util.List;

public class MissingPropertiesData {

	private String uri;
	private List<String> path;
	private List<String> props;

	public MissingPropertiesData(String uri, List<String> path, List<String> props) {
		super();
		this.uri = uri;
		this.path = path;
		this.props = props;
	}

	public MissingPropertiesData() {
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public List<String> getPath() {
		return path;
	}
	public void setPath(List<String> path) {
		this.path = path;
	}
	public List<String> getProps() {
		return props;
	}
	public void setProps(List<String> props) {
		this.props = props;
	}

	@Override
	public String toString() {
		return "MissingPropertiesData [uri=" + uri + ", path=" + path + ", props=" + props + "]";
	}
}
