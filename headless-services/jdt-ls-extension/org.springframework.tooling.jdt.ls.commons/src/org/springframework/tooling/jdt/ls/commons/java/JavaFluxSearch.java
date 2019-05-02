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
package org.springframework.tooling.jdt.ls.commons.java;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.resources.ResourceUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class JavaFluxSearch {
	
	final private Logger logger;
	final private JavaData javaData;
	final private Cache<Tuple2<Boolean, Boolean>, PackageFluxSearch> packageSearchCache = CacheBuilder.newBuilder().build();
	final private Cache<Tuple2<Boolean, Boolean>, TypeFluxSearch> typeSearchCache = CacheBuilder.newBuilder().build();

	public JavaFluxSearch(Logger logger, JavaData javaData) {
		super();
		this.logger = logger;
		this.javaData = javaData;
	}

	public List<String> fuzzySearchPackages(JavaSearchParams params) throws Exception {
		URI projectUri = params.getProjectUri() == null ? null : URI.create(params.getProjectUri());
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);

		PackageFluxSearch fluxPackageSearch = packageSearchCache.get(
				Tuples.of(params.isIncludeBinaries(), params.isIncludeSystemLibs()), () -> new PackageFluxSearch(logger, params.isIncludeBinaries(), params.isIncludeSystemLibs())
		);
		return fluxPackageSearch.searchWithLimits(javaProject, params.getTerm(), params.getSearchType(), params.getTimeLimit());
	}

	public List<TypeDescriptorData> fuzzySearchTypes(JavaSearchParams params) throws Exception {
		URI projectUri = params.getProjectUri() == null ? null : URI.create(params.getProjectUri());
		IJavaProject javaProject = projectUri == null ? null : ResourceUtils.getJavaProject(projectUri);
		
		TypeFluxSearch fluxTypeSearch = typeSearchCache.get(
				Tuples.of(params.isIncludeBinaries(), params.isIncludeSystemLibs()), () -> new TypeFluxSearch(logger, javaData, params.isIncludeBinaries(), params.isIncludeSystemLibs())
		);
		return fluxTypeSearch.searchWithLimits(javaProject, params.getTerm(), params.getSearchType(), params.getTimeLimit());
	}
	
}
