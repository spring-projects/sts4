/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.reconcile;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class MissingPropertiesData {

	private String uri;

	/**
	 * Yaml path (encoded segments) pointing to the node where
	 * the missing properties should be added (as children of that node).
	 */
	private List<String> path;

	/**
	 * The properties that are missing and should be added.
	 */
	private List<String> props;

	/**
	 * Snippet to insert when applying the quickfix.
	 */
	private String snippet;

	/**
	 * Offset where to place cursor relative to snippet start.
	 */
	private int cursorOffset;

	public MissingPropertiesData() {
	}

	public MissingPropertiesData(String uri, List<String> path, List<String> props, String snippet, int cursorOffset) {
		super();
		this.uri = uri;
		this.path = path;
		this.props = props;
		this.snippet = snippet;
		this.cursorOffset = cursorOffset;
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

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	@Override
	public String toString() {
		return "MissingPropertiesData [uri=" + uri + ", path=" + path + ", props=" + props + "]";
	}

	public int getCursorOffset() {
		return cursorOffset;
	}

	public void setCursorOffset(int cursorOffset) {
		this.cursorOffset = cursorOffset;
	}
}
