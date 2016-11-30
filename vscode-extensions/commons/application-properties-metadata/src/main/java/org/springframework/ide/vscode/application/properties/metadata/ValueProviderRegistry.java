/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties.metadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.boot.configurationmetadata.ValueProvider;
import org.springframework.ide.vscode.application.properties.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.CollectionUtil;

import reactor.core.publisher.Flux;

/**
 * An instance of this class serves as a 'registry' that associates known
 * {@link ValueProvider} ids to strategy objects used in the computation of completions
 * for properties to which the provider is attached.
 *
 * @author Kris De Volder
 */
public class ValueProviderRegistry {

	private static ValueProviderRegistry DEFAULT;

	/**
	 * Creates a default {@link ValueProviderRegistry} which is initialized with all the known
	 * providers. (This is the one production code should use, test code might make use
	 * something else for mocking purposes).
	 */
	public synchronized static ValueProviderRegistry getDefault() {
		if (DEFAULT==null) {
			DEFAULT = new ValueProviderRegistry();
			DEFAULT.initializeDefaults(DEFAULT);
		}
		return DEFAULT;
	}

	protected void initializeDefaults(ValueProviderRegistry r) {
//		def("logger-name", LoggerNameProvider.FACTORY);
		def("class-reference", ClassReferenceProvider.FACTORY);
	}

	private Map<String, Function<Map<String, Object>, ValueProviderStrategy>> registry = new HashMap<>();

	public interface ValueProviderStrategy {
		Flux<StsValueHint> getValues(IJavaProject javaProject, String query);

		default Collection<StsValueHint> getValuesNow(IJavaProject javaProject, String query) {
			return this.getValues(javaProject, query)
			.take(CachingValueProvider.TIMEOUT)
			.collectList()
			.block();
		}
	}

	/**
	 * Defines a value provider by binding its id to a strategy.
	 */
	public void def(String id, Function<Map<String, Object>, ValueProviderStrategy> algo) {
		registry.put(id, algo);
	}

	/**
	 * Resolve a list of {@link ValueProvider}s to a {@link ValueProviderStrategy}.
	 * <p>
	 * Essentially this finds the first provider from the list which has a known name
	 * and uses that to iinstantiate a ValueProviderStrategy. Spring boot assumes that
	 * a list is provided to allow new providers to be defined that override older ones
	 * and these are added at the top of the list. Thus an older IDE can continue to
	 * function using the older provider further down the list whereas newer IDEs will
	 * use a 'better' one from higher up the list.
	 */
	public ValueProviderStrategy resolve(List<ValueProvider> providerDescriptors) {
		if (CollectionUtil.hasElements(providerDescriptors)) {
			for (ValueProvider descriptor : providerDescriptors) {
				Function<Map<String, Object>, ValueProviderStrategy> factory = registry.get(descriptor.getName());
				if (factory!=null) {
					Map<String, Object> params = descriptor.getParameters();
					return factory.apply(params);
				}
			}
		}
		return null;
	}

}
