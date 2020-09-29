/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.async;

import java.util.function.Function;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchPattern;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Searches for all constructors for a given type.
 */
public class ConstructorSearchValueProvider extends CachingValueProvider<JavaConstructorHint> {

	@Override
	public Flux<JavaConstructorHint> getValuesAsycn(IType type, IJavaSearchScope scope) {
		try {
			return new FluxConstructorSearch()
					.scope(scope)
					.pattern("*")
					.patternRule(SearchPattern.R_PATTERN_MATCH)
					.search()
					.flatMap(getPostProcessor());
		} catch (Exception e) {
			return Flux.error(e);
		}
	}

	protected Function<JavaConstructorHint, Mono<JavaConstructorHint>> getPostProcessor() {
		return (match) -> {
			return Mono.justOrEmpty(match);
		};
	}
}
