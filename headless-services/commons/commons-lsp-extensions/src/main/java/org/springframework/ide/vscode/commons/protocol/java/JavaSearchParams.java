/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.protocol.java;

public class JavaSearchParams {
	
	private String projectUri;
	private String term;
	private boolean includeBinaries;
	private boolean includeSystemLibs;
	
	public JavaSearchParams(String projectUri, String term) {
		this(projectUri, term, true, false);
	}

	public JavaSearchParams(String projectUri, String term, boolean includeBinaries, boolean includeSystemLibs) {
		super();
		this.projectUri = projectUri;
		this.term = term;
		this.includeBinaries = includeBinaries;
		this.includeSystemLibs = includeSystemLibs;
	}

	public String getProjectUri() {
		return projectUri;
	}

	public void setProjectUri(String projectUri) {
		this.projectUri = projectUri;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public boolean isIncludeBinaries() {
		return includeBinaries;
	}

	public void setIncludeBinaries(boolean includeBinaries) {
		this.includeBinaries = includeBinaries;
	}

	public boolean isIncludeSystemLibs() {
		return includeSystemLibs;
	}

	public void setIncludeSystemLibs(boolean includeSystemLibs) {
		this.includeSystemLibs = includeSystemLibs;
	}

	
}
