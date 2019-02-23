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
package org.springframework.ide.vscode.commons.jdtls;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.ClasspathIndex;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.JdtLsJavadocProvider;
import org.springframework.ide.vscode.commons.protocol.STS4LanguageClient;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;

import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class JdtLsIndex implements ClasspathIndex {

	private static final Logger log = LoggerFactory.getLogger(JdtLsIndex.class);

	private final STS4LanguageClient client;
	private final URI projectUri;
	private final JdtLsJavadocProvider javadocProvider;

	private Cache<String, Optional<IType>> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

	public JdtLsIndex(STS4LanguageClient client, URI projectUri) {
		this.client = client;
		this.projectUri = projectUri;
		this.javadocProvider = new JdtLsJavadocProvider(client, projectUri.toString());
	}

	@Override
	public void dispose() {
	}

	private IType toType(TypeData data) {
		String declaringTypeBindingKey = data.getDeclaringType();
		String declaringTypeFqName = declaringTypeBindingKey == null ? null : declaringTypeBindingKey.substring(1, declaringTypeBindingKey.length() - 1).replace('/', '.');
		return Wrappers.wrap(data, Suppliers.memoize(() -> declaringTypeFqName == null ? null : findType(declaringTypeFqName)), javadocProvider);
	}

	@Override
	public IType findType(String fqName) {
		try {
			return cache.get(fqName, () -> {
				JavaDataParams params = new JavaDataParams(projectUri.toString(), "L" + fqName.replace('.', '/') + ";", false);
				try {
					TypeData data = client.javaType(params).get(500, TimeUnit.MILLISECONDS);
					if (data != null) {
						return Optional.ofNullable(toType(data));
					}
				} catch (InterruptedException | ExecutionException | TimeoutException e) {
					log.error("", e);
				}
				return Optional.empty();
			}).orElse(null);
		} catch (ExecutionException e) {
			log.error("{}", e);
			return null;
		}
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, boolean includeBinaries, boolean includeSystemLibs, Predicate<IType> typeFilter) {
		JavaSearchParams searchParams = new JavaSearchParams(projectUri.toString(), searchTerm, includeBinaries, includeSystemLibs);
		return Mono.fromFuture(client.javaSearchTypes(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(this::toType)
				.filter(t -> typeFilter == null || typeFilter.test(t))
				.map(type -> Tuples.of(type,  FuzzyMatcher.matchScore(searchTerm, type.getFullyQualifiedName())))
				.filter(tuple -> tuple.getT2() != 0.0);
	}

	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm, boolean includeBinaries, boolean includeSystemLibs) {
		JavaSearchParams searchParams = new JavaSearchParams(projectUri.toString(), searchTerm, includeBinaries, includeSystemLibs);
		return Mono.fromFuture(client.javaSearchPackages(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(p -> Tuples.of(p, FuzzyMatcher.matchScore(searchTerm, p)))
				.filter(tuple -> tuple.getT2() != 0.0);
	}

	@Override
	public Flux<IType> allSubtypesOf(IType type) {
		JavaTypeHierarchyParams searchParams = new JavaTypeHierarchyParams(projectUri.toString(), type.getFullyQualifiedName());
		return Mono.fromFuture(client.javaSubTypes(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(this::toType);
	}

	@Override
	public Flux<IType> allSuperTypesOf(IType type) {
		JavaTypeHierarchyParams searchParams = new JavaTypeHierarchyParams(projectUri.toString(), type.getFullyQualifiedName());
		return Mono.fromFuture(client.javaSuperTypes(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(this::toType);
	}

	@Override
	public IJavaModuleData findClasspathResourceContainer(String fqName) {
		IType type = findType(fqName);
		return type == null ? null : type.classpathContainer();
	}

}
