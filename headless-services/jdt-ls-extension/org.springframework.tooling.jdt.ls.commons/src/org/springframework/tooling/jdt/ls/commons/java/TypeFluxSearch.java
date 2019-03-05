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

import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.searchScope;
import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.toProperTypeQuery;
import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.toTypePattern;
import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.toWildCardPattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.tooling.jdt.ls.commons.Logger;

import reactor.core.publisher.Flux;

public class TypeFluxSearch extends CachingFluxJavaSearch<String> {

	public TypeFluxSearch(Logger logger, boolean includeBinaries, boolean includeSystemLibs) {
		super(logger, includeBinaries, includeSystemLibs);
	}

	@Override
	protected Flux<String> getValuesAsync(IJavaProject javaProject, String searchTerm) {
		try {
			return new FluxJdtSearch(logger)
				.scope(searchScope(javaProject, includeBinaries, includeSystemLibs))
				.pattern(toTypePattern(toWildCardPattern(toProperTypeQuery(searchTerm))))
				.search()
				.map(match -> match.getElement())
				.filter(o -> o instanceof IType)
				.map(e -> ((IType) e).getFullyQualifiedName());
		} catch (Exception e) {
			logger.log(e);
			return Flux.empty();
		}
	}

	@Override
	protected String stringValue(String t) {
		return t;
	}

}
