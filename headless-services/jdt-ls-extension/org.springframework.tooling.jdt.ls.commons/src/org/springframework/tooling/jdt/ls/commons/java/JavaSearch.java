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

import java.net.URI;
import java.util.stream.Stream;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

public class JavaSearch {
	
	private Logger logger;
	
	public JavaSearch(Logger logger) {
		super();
		this.logger = logger;
	}
	
	public Stream<String> fuzzySearchPackages(URI projectUri, String searchTerm, boolean includeBinaries, boolean includeSystemLibs) throws Exception {
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
		return new StreamJdtSearch(logger)
			.scope(StreamJdtSearch.searchScope(javaProject, includeBinaries, includeSystemLibs))
			.pattern(StreamJdtSearch.toPackagePattern(StreamJdtSearch.toWildCardPattern(searchTerm)))
			.search()
			.parallel()
			.map(match -> match.getElement())
			.filter(o -> o instanceof IPackageFragment)
			.map(p -> ((IPackageFragment)p).getElementName());
	}
	
	public Stream<String> fuzzySearchTypes(URI projectUri, String searchTerm, boolean includeBinaries, boolean includeSystemLibs) throws Exception {
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
		return new StreamJdtSearch(logger)
			.scope(StreamJdtSearch.searchScope(javaProject, includeBinaries, includeSystemLibs))
			.pattern(StreamJdtSearch.toTypePattern(StreamJdtSearch.toWildCardPattern(StreamJdtSearch.toProperTypeQuery(searchTerm))))
			.search()
			.parallel()
			.map(match -> match.getElement())
			.filter(o -> o instanceof IType)
			.map(e -> ((IType) e).getFullyQualifiedName());
	}
	
}
