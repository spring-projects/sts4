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
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.vscode.commons.util.Log;

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
	
	public static final String CLASSPATH_DATA_CACHE_FILE = "classpath-data.json";
	private static final ClasspathData EMPTY_CLASSPATH_DATA = new ClasspathData(null, Collections.emptySet(),
			Collections.emptySet(), null);
	
	private static final String OUTPUT_FOLDER_PROPERTY = "outputFolder";
	private static final String CLASSPATH_RESOURCES_PROPERTY = "classpathResources";
	private static final String CLASSPATH_ENTRIES_PROPERTY = "classpathEntries";
	private static final String NAME_PROPERTY = "name";

	protected static class ClasspathData {
		
		final public String name;
		final public Set<Path> classpathEntries;
		final public Set<String> classpathResources;
		final public Path outputFolder;
		
		public ClasspathData(String name, Set<Path> classpathEntries, Set<String> classpathResources, Path outputFolder) {
			this.name = name;
			this.classpathEntries = classpathEntries;
			this.classpathResources = classpathResources;
			this.outputFolder = outputFolder;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ClasspathData) {
				ClasspathData other = (ClasspathData) obj;
				try {
					return Objects.equal(name, other.name)
							&& Objects.equal(classpathEntries, other.classpathEntries)
							&& Objects.equal(classpathResources, other.classpathResources)
							&& Objects.equal(outputFolder, outputFolder);
				} catch (Throwable t) {
					Log.log(t);
				}
			}
			return false;
		}
		
	}
	
	private AtomicReference<ClasspathData> cachedData;
	private Callable<T> delegateCreator;
	private AtomicReference<T> cachedDelegate;
	
	final private File cacheFile;
	
	public DelegatingCachedClasspath(Callable<T> delegateCreator, File cacheFile) {
		super();
		this.cacheFile = cacheFile;
		this.cachedDelegate = new AtomicReference<>(null);
		this.cachedData = new AtomicReference<>(init());
		this.delegateCreator = delegateCreator;
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
		return cacheFile != null && cacheFile.exists();
	}
	
	private synchronized ClasspathData loadCachedData() {
		if (cacheFile != null && cacheFile.exists()) {
			try {
				JSONObject json = new JSONObject(new JSONTokener(new FileInputStream(cacheFile)));
				String name = json.getString(NAME_PROPERTY);
				JSONArray classpathEntriesJson = json.optJSONArray(CLASSPATH_ENTRIES_PROPERTY);
				JSONArray classpathResourcesJson = json.optJSONArray(CLASSPATH_RESOURCES_PROPERTY);
				String outputFolderStr = json.optString(OUTPUT_FOLDER_PROPERTY);
				
				return new ClasspathData(
						name,
						classpathEntriesJson == null ? Collections.emptySet() : classpathEntriesJson.toList().stream()
								.filter(o -> o instanceof String)
								.map(o -> (String) o)
								.map(s -> new File(s).toPath())
								.collect(Collectors.toSet()),
						classpathResourcesJson == null ? Collections.emptySet() : classpathResourcesJson.toList().stream()
								.filter(o -> o instanceof String)
								.map(o -> (String) o)
								.collect(Collectors.toSet()),
						outputFolderStr == null ? null : new File(outputFolderStr).toPath()
					);
			} catch (Throwable e) {
				Log.log(e);
			}
		}
		return null;
	}
	
	private ClasspathData init() {
		ClasspathData data = loadCachedData();
		return data == null ? new ClasspathData(null, Collections.emptySet(), Collections.emptySet(), null) : data;
	}
	
	private synchronized void persistCachedData(ClasspathData data) {
		if (cacheFile != null && data != null) {
			FileWriter writer = null;
			try {
				Files.createDirectories(cacheFile.getParentFile().toPath());
				JSONObject json = new JSONObject();
				json.put(NAME_PROPERTY, data.name);
				json.put(CLASSPATH_ENTRIES_PROPERTY, data.classpathEntries.stream().map(e -> e.toString()).collect(Collectors.toList()));
				json.put(CLASSPATH_RESOURCES_PROPERTY, data.classpathResources);
				json.put(OUTPUT_FOLDER_PROPERTY, data.outputFolder);
				writer = new FileWriter(cacheFile);
				json.write(writer);
			} catch (IOException e) {
				Log.log(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {
						Log.log(e);
					}
				}
			}
		}
	}
	
	public boolean update() throws Exception {
		try {
			final ClasspathData newData = createClasspathData();
			if (!Objects.equal(cachedData.get(), newData)) {
				cachedData.set(newData);
				persistCachedData(newData);
				return true;
			}
			return false;
		} catch (Exception e) {
			cachedData.set(new ClasspathData(null, Collections.emptySet(), Collections.emptySet(), null));
			if (cacheFile != null && cacheFile.exists()) {
				cacheFile.delete();
			}
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

	protected ClasspathData createClasspathData() throws Exception {
		T newDelegate = delegateCreator.call();
		cachedDelegate.set(newDelegate);
		if (newDelegate == null) {
			return EMPTY_CLASSPATH_DATA;
		} else {
			LinkedHashSet<Path> classpathEntries = new LinkedHashSet<>(newDelegate.getClasspathEntries());
			return new ClasspathData(newDelegate.getName(), classpathEntries,
					new LinkedHashSet<>(newDelegate.getClasspathResources()), newDelegate.getOutputFolder());
		}
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
