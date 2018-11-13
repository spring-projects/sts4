/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.metadata;

import java.util.Map;
import java.util.function.Function;

import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.vscode.boot.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuples;

/**
 * Provides the algorithm for 'logger-name' valueProvider.
 * <p>
 * See: https://github.com/spring-projects/spring-boot/blob/master/spring-boot-docs/src/main/asciidoc/appendix-configuration-metadata.adoc
 *
 * @author Kris De Volder
 * @author Alex Boyko
 */
public class LoggerNameProvider extends CachingValueProvider {

	private final SpringPropertyIndexProvider adhocProperties;

	public LoggerNameProvider(SpringPropertyIndexProvider adhocProperties) {
		this.adhocProperties = adhocProperties;
	}

	public final Function<Map<String, Object>, ValueProviderStrategy> FACTORY = (params) -> this;

	@Override
	protected Flux<StsValueHint> getValuesAsync(IJavaProject javaProject, String query) {
		return Flux.concat(
			javaProject.getIndex()
				.fuzzySearchPackages(query)
				.map(t -> Tuples.of(StsValueHint.create(t.getT1()), t.getT2())),
			javaProject.getIndex()
				.fuzzySearchTypes(query, null)
				.map(t -> Tuples.of(StsValueHint.create(javaProject, t.getT1()), t.getT2()))
			)
		.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
		.flatMapIterable(l -> l)
		.map(t -> t.getT1());
	}
}
