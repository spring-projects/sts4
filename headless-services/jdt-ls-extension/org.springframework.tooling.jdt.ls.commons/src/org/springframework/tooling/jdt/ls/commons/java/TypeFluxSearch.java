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
import static org.springframework.tooling.jdt.ls.commons.java.SearchUtils.toTypePattern;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;
import org.springframework.tooling.jdt.ls.commons.Logger;

import reactor.core.publisher.Flux;

public class TypeFluxSearch extends CachingFluxJavaSearch<TypeDescriptorData> {

	private JavaData javaData;

	public TypeFluxSearch(Logger logger, JavaData javaData, boolean includeBinaries, boolean includeSystemLibs) {
		super(logger, includeBinaries, includeSystemLibs);
		this.javaData = javaData;
	}

	@Override
	protected Flux<TypeDescriptorData> getValuesAsync(IJavaProject javaProject, String searchTerm, String searchType) {
		try {
			return new FluxJdtSearch(logger)
				.scope(searchScope(javaProject, includeBinaries, includeSystemLibs))
				.pattern(toTypePattern(searchType, searchTerm))
				.search()
				.map(match -> match.getElement())
				.filter(o -> o instanceof IType)
				.map(e -> javaData.createTypeDescriptorData((IType) e));
		} catch (Exception e) {
			logger.log(e);
			return Flux.empty();
		}
	}
	
	@Override
	protected String stringValue(TypeDescriptorData t) {
		return t.getFqName();
	}

}
