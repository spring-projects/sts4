/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.jdtls;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.ClasspathIndex;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.JavaUtils;
import org.springframework.ide.vscode.commons.javadoc.JdtLsJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.protocol.STS4LanguageClient;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams;
import org.springframework.ide.vscode.commons.protocol.java.JavaSearchParams.SearchType;
import org.springframework.ide.vscode.commons.protocol.java.JavaTypeHierarchyParams;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeDescriptorData;
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

	final private static long SEARCH_TIMEOUT = 700;

	final private static Logger log = LoggerFactory.getLogger(JdtLsIndex.class);

	final private STS4LanguageClient client;
	final private URI projectUri;
	final private JdtLsJavadocProvider javadocProvider;
	final private ProjectObserver projectObserver;

	final private Cache<String, Optional<IType>> binaryTypeCache = CacheBuilder.newBuilder().build();
	final private Cache<String, Optional<IType>> sourceTypeCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

	final private Cache<JavaTypeHierarchyParams, CompletableFuture<List<IType>>> supertypesCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
	final private Cache<JavaTypeHierarchyParams, CompletableFuture<List<IType>>> subtypesCache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
	
	final private Listener projectListener;

	public JdtLsIndex(STS4LanguageClient client, URI projectUri, ProjectObserver projectObserver) {
		this.client = client;
		this.projectUri = projectUri;
		this.projectObserver = projectObserver;
		this.javadocProvider = new JdtLsJavadocProvider(client, projectUri.toString());
		
		this.projectListener = ProjectObserver.onAny(project -> {
			if (Objects.equals(project.getLocationUri(), projectUri)) {
				binaryTypeCache.invalidateAll();
				sourceTypeCache.invalidateAll();
				supertypesCache.invalidateAll();
				subtypesCache.invalidateAll();
			}
		});

		this.projectObserver.addListener(projectListener);
	}

	@Override
	public void dispose() {
		projectObserver.removeListener(projectListener);
	}

	private IType toType(TypeData data) {
		String declaringTypeBindingKey = data.getDeclaringType();
		String declaringTypeFqName = JavaUtils.typeBindingKeyToFqName(declaringTypeBindingKey);
		return Wrappers.wrap(data, Suppliers.memoize(() -> declaringTypeFqName == null ? null : findType(declaringTypeFqName)), javadocProvider);
	}

	private IType toTypeFromDescriptor(TypeDescriptorData data) {
		String declaringTypeBindingKey = data.getDeclaringType();
		String declaringTypeFqName = JavaUtils.typeBindingKeyToFqName(declaringTypeBindingKey);
		return Wrappers.wrap(data, Suppliers.memoize(() -> findType(data.getFqName())), Suppliers.memoize(() -> declaringTypeFqName == null ? null : findType(declaringTypeFqName)), javadocProvider);
	}

	private TypeData findTypeData(String fqName) throws InterruptedException, ExecutionException {
		JavaDataParams params = new JavaDataParams(projectUri.toString(), "L" + fqName.replace('.', '/') + ";", false);
		return client.javaType(params).get();
	}

	@Override
	public IType findType(String fqName) {
		Optional<IType> type = binaryTypeCache.getIfPresent(fqName);
		if (type == null) {
			type = sourceTypeCache.getIfPresent(fqName);
		}

		if (type == null) {
			try {
				TypeData data = findTypeData(fqName);
				if (data == null) {
					type = Optional.empty();
					sourceTypeCache.put(fqName, type);
				} else {
					type = Optional.of(toType(data));
					if (data.getClasspathEntry() != null && Classpath.isBinary(data.getClasspathEntry().getCpe())) {
						binaryTypeCache.put(fqName, type);
					} else {
						sourceTypeCache.put(fqName, type);
					}
				}
			} catch (Exception e) {
				log.error("Find type failed for `{}` with {}", fqName, e);
				return null;
			}
		}
		return type.orElse(null);
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, boolean includeBinaries, boolean includeSystemLibs) {
		JavaSearchParams searchParams = new JavaSearchParams(projectUri.toString(), searchTerm, SearchType.FUZZY, includeBinaries, includeSystemLibs, SEARCH_TIMEOUT);
		return Mono.fromFuture(client.javaSearchTypes(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(type -> Tuples.of(toTypeFromDescriptor(type),  FuzzyMatcher.matchScore(searchTerm, type.getFqName())))
				.filter(tuple -> tuple.getT2() != 0.0);
	}

	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm, boolean includeBinaries, boolean includeSystemLibs) {
		JavaSearchParams searchParams = new JavaSearchParams(projectUri.toString(), searchTerm, SearchType.FUZZY, includeBinaries, includeSystemLibs, SEARCH_TIMEOUT);
		return Mono.fromFuture(client.javaSearchPackages(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(p -> Tuples.of(p, FuzzyMatcher.matchScore(searchTerm, p)))
				.filter(tuple -> tuple.getT2() != 0.0);
	}
	
	@Override
	public Flux<Tuple2<IType, Double>> camelcaseSearchTypes(String searchTerm, boolean includeBinaries,
			boolean includeSystemLibs) {
		JavaSearchParams searchParams = new JavaSearchParams(projectUri.toString(), searchTerm, SearchType.CAMELCASE, includeBinaries, includeSystemLibs, SEARCH_TIMEOUT);
		return Mono.fromFuture(client.javaSearchTypes(searchParams))
				.flatMapMany(results -> Flux.fromIterable(results).publishOn(Schedulers.parallel()))
				.filter(Objects::nonNull)
				.map(type -> Tuples.of(toTypeFromDescriptor(type),  FuzzyMatcher.matchScore(searchTerm, type.getFqName())))
				.filter(tuple -> tuple.getT2() != 0.0);
	}

	@Override
	public Flux<IType> allSubtypesOf(String fqName, boolean includeFocusType, boolean detailed) {
		JavaTypeHierarchyParams searchParams = new JavaTypeHierarchyParams(projectUri.toString(), fqName, includeFocusType, detailed);
		try {
			CompletableFuture<List<IType>> future = subtypesCache.get(searchParams, () -> client
					.javaSubTypes(searchParams)
					.handle((results, exception) -> results.stream().map(e -> detailed ? toType(e.getRight()) : toTypeFromDescriptor(e.getLeft())).collect(Collectors.toList())));
			return Mono.fromFuture(future)
					.flatMapMany(results -> Flux.fromIterable(results));
		} catch (ExecutionException e) {
			log.error("{}", e);
			return Flux.empty();
		}
	}

	@Override
	public Flux<IType> allSuperTypesOf(String fqName, boolean includeFocusType, boolean detailed) {
		JavaTypeHierarchyParams searchParams = new JavaTypeHierarchyParams(projectUri.toString(), fqName, includeFocusType, detailed);
		try {
			CompletableFuture<List<IType>> future = supertypesCache.get(searchParams, () -> client
					.javaSuperTypes(searchParams)
					.handle((results, exception) -> results.stream().map(e -> detailed ? toType(e.getRight()) : toTypeFromDescriptor(e.getLeft())).collect(Collectors.toList())));
			return Mono.fromFuture(future)
					.flatMapMany(results -> Flux.fromIterable(results));
		} catch (ExecutionException e) {
			log.error("{}", e);
			return Flux.empty();
		}
	}

	@Override
	public IJavaModuleData findClasspathResourceContainer(String fqName) {
		IType type = findType(fqName);
		return type == null ? null : type.classpathContainer();
	}

}
