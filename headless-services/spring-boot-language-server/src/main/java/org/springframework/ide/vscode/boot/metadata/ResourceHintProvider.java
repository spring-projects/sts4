/*******************************************************************************
 * Copyright (c) 2016, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.metadata;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.vscode.boot.metadata.hints.StsValueHint;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;

/**
 * @author Kris De Volder
 */
public class ResourceHintProvider implements ValueProviderStrategy {

	private static String[] CLASSPATH_PREFIXES = {
			"classpath:",
			"classpath*:"
	};

	private static final String[] URL_PREFIXES = new String[] {
			"classpath:",
			"classpath*:",
			"file:",
			"http://",
			"https://"
	};

	@Override
	public Flux<StsValueHint> getValues(IJavaProject javaProject, String query) {
		for (String prefix : CLASSPATH_PREFIXES) {
			if (query.startsWith(prefix)) {
				return classpathHints
				.getValues(javaProject, query.substring(prefix.length()))
				.map((hint) -> hint.prefixWith(prefix));
			}
		}
		return Flux.fromIterable(urlPrefixHints);
	}

	final private ImmutableList<StsValueHint> urlPrefixHints = ImmutableList.copyOf(
			Arrays.stream(URL_PREFIXES)
			.map(StsValueHint::create)
			.collect(Collectors.toList())
	);

	private ClasspathHints classpathHints = new ClasspathHints();

	private static class ClasspathHints extends CachingValueProvider {
		@Override
		protected Flux<StsValueHint> getValuesAsync(IJavaProject javaProject, String query) {
			return Flux.fromStream(
				IClasspathUtil.getClasspathResources(javaProject.getClasspath()).stream()
				.distinct().map(r -> r.replaceAll("\\\\", "/"))
				.map(StsValueHint::create)
			);
		}
	}


}
