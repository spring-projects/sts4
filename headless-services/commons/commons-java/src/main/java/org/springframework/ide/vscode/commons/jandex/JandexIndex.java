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

package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class JandexIndex extends BasicJandexIndex {

	private static final Logger log = LoggerFactory.getLogger(JandexIndex.class);

	@FunctionalInterface
	public static interface JavadocProviderFactory {
		IJavadocProvider createJavadocProvider(File jarContainer);
	}

	private JavadocProviderFactory javadocProviderFactory;

	private Cache<File, IJavadocProvider> javadocProvidersCache = CacheBuilder.newBuilder().build();

	public void setJvadocProviderFactory(JavadocProviderFactory sourceContainerProvider) {
		this.javadocProviderFactory = sourceContainerProvider;
	}

	public JavadocProviderFactory getJavadocProviderFactory() {
		return javadocProviderFactory;
	}

	public JandexIndex(IClasspath classpath, IndexFileFinder indexFileFinder,
			JavadocProviderFactory javadocProviderFactory) {
		super(classpath, indexFileFinder);
		this.javadocProviderFactory = javadocProviderFactory;
	}

	public IType findType(String fqName) {
		Tuple2<IJavaModuleData, ClassInfo> result = getClassByName(DotName.createSimple(fqName));
		return result == null ? null : createType(result);
	}

	private IType createType(Tuple2<IJavaModuleData, ClassInfo> match) {
		if (match == null) {
			return null;
		}
		File classpathResource = match.getT1().getContainer();
		IJavadocProvider javadocProvider = null;
		try {
			javadocProvider = javadocProvidersCache.get(classpathResource, () -> {
				IJavadocProvider provider = null;
				if (javadocProviderFactory != null) {
					provider = javadocProviderFactory.createJavadocProvider(classpathResource);
				}
				return provider == null ? IJavadocProvider.NULL : provider;
			});
		} catch (ExecutionException e) {
			log.error("Failed to retrieve javadoc provider for resource " + classpathResource, e);
		}
		return Wrappers.wrap(this, match.getT1(), match.getT2(), javadocProvider);
	}

	Flux<Tuple2<IType, Double>> fuzzySearchITypes(String searchTerm) {
		return fuzzySearchTypes(searchTerm).map(m -> Tuples.of(createType(Tuples.of(m.getT1(), m.getT2())), m.getT3()));
	}

	public Flux<IType> allSubtypesOf(IType type) {
		DotName name = DotName.createSimple(type.getFullyQualifiedName());
		return allSubtypesOf(name, type.isInterface()).map(match -> createType(match));
	}

}
