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

public class JavaSearchParams {
	
	public final class SearchType {
		public static final String FUZZY = "fuzzy";
		public static final String CAMELCASE = "camelcase";
	}
	
	private String projectUri;
	private String term;
	private boolean includeBinaries;
	private boolean includeSystemLibs;
	private long timeLimit = -1;
	private String searchType;
	
	public JavaSearchParams(String projectUri, String term, String searchType) {
		this(projectUri, term, searchType, true, false);
	}

	public JavaSearchParams(String projectUri, String term, String searchType, boolean includeBinaries, boolean includeSystemLibs, long timeLimit) {
		super();
		this.projectUri = projectUri;
		this.term = term;
		this.setSearchType(searchType);
		this.includeBinaries = includeBinaries;
		this.includeSystemLibs = includeSystemLibs;
		this.setTimeLimit(timeLimit);
	}

	public JavaSearchParams(String projectUri, String term, String searchType, boolean includeBinaries, boolean includeSystemLibs) {
		this(projectUri, term, searchType, includeBinaries, includeSystemLibs, -1);
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

	public long getTimeLimit() {
		return timeLimit;
	}

	public void setTimeLimit(long timeLimit) {
		this.timeLimit = timeLimit;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

}
