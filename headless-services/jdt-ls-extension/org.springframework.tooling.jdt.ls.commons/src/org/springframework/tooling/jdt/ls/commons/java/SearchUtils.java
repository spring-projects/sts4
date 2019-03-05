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
package org.springframework.tooling.jdt.ls.commons.java;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;

public class SearchUtils {
	
	public static String toWildCardPattern(String query) {
		StringBuilder builder = new StringBuilder("*");
		for (char c : query.toCharArray()) {
			builder.append(c);
			builder.append('*');
		}
		return builder.toString();
	}

	public static SearchPattern toPackagePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.PACKAGE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toClassPattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.CLASS;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static SearchPattern toTypePattern(String wildCardedQuery) {
		int searchFor = IJavaSearchConstants.TYPE;
		int limitTo = IJavaSearchConstants.DECLARATIONS;
		int matchRule = SearchPattern.R_PATTERN_MATCH;
		return SearchPattern.createPattern(wildCardedQuery, searchFor, limitTo, matchRule);
	}

	public static String toProperTypeQuery(String query) {
		int idx = query.lastIndexOf('.');
		if (idx > 0 && idx < query.length() - 1 && Character.isLowerCase(query.charAt(idx + 1))) {
			return query + '.';
		} else {
			return query;
		}
	}

	/**
	 * Create a search scope that includes a given project and its dependencies.
	 */
	public static IJavaSearchScope searchScope(IJavaProject javaProject, boolean includeBinaries, boolean includeSystemLibs) throws JavaModelException {
		int includeMask =
				IJavaSearchScope.REFERENCED_PROJECTS |
				IJavaSearchScope.SOURCES;
		if (includeBinaries) {
			includeMask = includeMask | IJavaSearchScope.APPLICATION_LIBRARIES;
		}
		if (includeSystemLibs) {
			includeMask = includeMask | IJavaSearchScope.SYSTEM_LIBRARIES; 
		}
		return SearchEngine.createJavaSearchScope(new IJavaElement[] {javaProject}, includeMask);
	}

}
