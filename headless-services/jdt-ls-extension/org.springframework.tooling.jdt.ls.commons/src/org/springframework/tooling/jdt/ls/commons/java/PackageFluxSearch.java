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

import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.searchScope;
import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.toPackagePattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.tooling.jdt.ls.commons.Logger;

import reactor.core.publisher.Flux;

public class PackageFluxSearch extends CachingFluxJavaSearch<String> {

	public PackageFluxSearch(Logger logger, boolean includeBinaries, boolean includeSystemLibs) {
		super(logger, includeBinaries, includeSystemLibs);
	}

	@Override
	protected Flux<String> getValuesAsync(IJavaProject javaProject, String searchTerm, String searchType) {
		try {
			return new FluxJdtSearch(logger)
					.scope(searchScope(javaProject, includeBinaries, includeSystemLibs))
					.pattern(toPackagePattern(searchType, searchTerm))
					.search()
					.map(match -> match.getElement())
					.filter(o -> o instanceof IPackageFragment)
					.map(p -> ((IPackageFragment)p).getElementName());
		} catch (JavaModelException e) {
			logger.log(e);
			return Flux.empty();
		}
	}

	@Override
	protected String stringValue(String t) {
		return t;
	}

}
