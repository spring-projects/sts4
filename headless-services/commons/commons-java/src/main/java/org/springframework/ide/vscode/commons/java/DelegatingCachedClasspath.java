/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import org.springframework.ide.vscode.commons.util.Assert;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * A wrapper around the classpath created from a Java project using the data in the project file (maven, gradle)
 * The wrapper caches some of classpath data such as
 * <li> Classpath entries </li>
 * <li> Classpath resources </li>
 * <li> Output folder </li>
 * <li> Projects' name </li>
 * 
 * The cached classpath data is written to ".sts4-cache/classpath-data.json" and loadedd from it when intance of this classpath is created
 * 
 * Implementation is somewhat experimental at the moment...
 * 
 * @author Alex Boyko
 *
 * @param <T> a subclass of {@link IClasspath} the delegated to classpath created from current data
 */
public class DelegatingCachedClasspath<T extends IClasspath> implements IClasspath {
	
	private AtomicReference<ClasspathData> cachedData;
	private Callable<T> delegateCreator;
	private AtomicReference<T> cachedDelegate;

	private final ClasspathFileBasedCache fileCache;
	
	
	public DelegatingCachedClasspath(Callable<T> delegateCreator, ClasspathFileBasedCache fileCache) {
		super();
		Assert.isLegal(delegateCreator != null);
		this.fileCache = fileCache != null ? fileCache : ClasspathFileBasedCache.NULL;
		this.cachedDelegate = new AtomicReference<>(null);
		this.cachedData = new AtomicReference<>(loadFileBasedCache(fileCache));
		this.delegateCreator = delegateCreator;
	}

	private ClasspathData loadFileBasedCache(ClasspathFileBasedCache fileCache) {
		return fileCache != null ? fileCache.load() : ClasspathData.EMPTY_CLASSPATH_DATA;
	}
	
	public T delegate() {
		return cachedDelegate.get();
	}

	@Override
	public String getName() {
		return cachedData.get().name;
	}

	@Override
	public Path getOutputFolder() {
		return cachedData.get().outputFolder;
	}

	@Override
	public ImmutableList<Path> getClasspathEntries() throws Exception {
		return ImmutableList.copyOf(cachedData.get().classpathEntries);
	}

	@Override
	public ImmutableList<String> getClasspathResources() {
		return ImmutableList.copyOf(cachedData.get().classpathResources);
	}
	
	public boolean isCached() {
		return fileCache.isCached();
	}
	
	public boolean update() throws Exception {
		try {
			final ClasspathData newData = createClasspathData();
			if (!Objects.equal(cachedData.get(), newData)) {
				cachedData.set(newData);
				fileCache.persist(newData);
				return true;
			}
			return false;
		} catch (Exception e) {
			cachedData.set(ClasspathData.EMPTY_CLASSPATH_DATA);
			fileCache.delete();
			throw e;
		}
	}
	
	@Override
	public boolean exists() {
		T t = cachedDelegate.get();
		return t != null && t.exists();
	}

	@Override
	public IType findType(String fqName) {
		T t = cachedDelegate.get();
		return t == null ? null : t.findType(fqName);
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter) {
		T t = cachedDelegate.get();
		return t == null ? Flux.empty() : t.fuzzySearchTypes(searchTerm, typeFilter);
	}

	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		T t = cachedDelegate.get();
		return t == null ? Flux.empty() : t.fuzzySearchPackages(searchTerm);
	}

	@Override
	public Flux<IType> allSubtypesOf(IType type) {
		T t = cachedDelegate.get();
		return t == null ? Flux.empty() : t.allSubtypesOf(type);
	}

	@Override
	public ClasspathData createClasspathData() throws Exception {
		T newDelegate = delegateCreator.call();
		cachedDelegate.set(newDelegate);
		if (newDelegate != null) {
			ClasspathData data = newDelegate.createClasspathData();
			if (data != null) {
				return data;
			}
		} 
		return ClasspathData.EMPTY_CLASSPATH_DATA;
	}

	@Override
	public ImmutableList<String> getSourceFolders() {
		T t = cachedDelegate.get();
		return t == null ? ImmutableList.of() : t.getSourceFolders();
	}

	@Override
	public Optional<File> findClasspathResourceContainer(String fqName) {
		T t = cachedDelegate.get();
		return t == null ? Optional.empty() : t.findClasspathResourceContainer(fqName);
	}
	
}
