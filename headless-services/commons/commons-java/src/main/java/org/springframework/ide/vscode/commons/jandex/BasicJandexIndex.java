/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Basic Jandex Index using only Reactor Flux and Jandex only constructs
 * independent of Javadoc provider logic. Thus this index can be shared via a
 * static object
 *
 * @author Alex Boyko
 *
 */
public class BasicJandexIndex {

	private static final Logger log = LoggerFactory.getLogger(BasicJandexIndex.class);

	private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

	@FunctionalInterface
	public static interface IndexFileFinder {
		File findIndexFile(File jarFile);
	}

	public static File getIndexFolder() {
		File folder = new File(System.getProperty(JAVA_IO_TMPDIR), "jandex");
		if (!folder.isDirectory()) {
			folder.mkdirs();
		}
		return folder;
	}

	private Map<File, Supplier<Optional<IndexView>>> index;

	private Map<File, Supplier<List<Tuple3<String, File, ClassInfo>>>> knownTypes;

	private Map<File, Supplier<List<String>>> knownPackages;

	private BasicJandexIndex[] baseIndex;

	BasicJandexIndex(Collection<File> classpathEntries, IndexFileFinder indexFileFinder,
			BasicJandexIndex... baseIndex) {
		this.baseIndex = baseIndex;
		this.index = new ConcurrentHashMap<>();
		this.knownTypes = new HashMap<>();
		this.knownPackages = new HashMap<>();
		classpathEntries.forEach(file -> {
			index.put(file, /*Suppliers.synchronizedSupplier(*/Suppliers.memoize(() -> createIndex(file, indexFileFinder))/*)*/);
			knownTypes.put(file, Suppliers.memoize(() -> getKnownTypesStream(file).collect(Collectors.toList())));
			knownPackages.put(file, Suppliers.memoize(() -> getKnownPackages(file).collect(Collectors.toList())));
		});
	}

	private Optional<IndexView> createIndex(File file, IndexFileFinder indexFileFinder) {
		if (file != null && file.isFile() && file.getName().endsWith(".jar")) {
			return indexJar(file, indexFileFinder);
		} else if (file != null && file.isDirectory()) {
			return indexFolder(file);
		} else {
			return Optional.empty();
		}
	}

	private static Optional<IndexView> indexFolder(File folder) {
		Indexer indexer = new Indexer();
		for (Iterator<File> itr = com.google.common.io.Files.fileTreeTraverser().breadthFirstTraversal(folder)
				.iterator(); itr.hasNext();) {
			File file = itr.next();
			if (file.isFile() && file.getName().endsWith(".class")) {
				try {
					final InputStream stream = new FileInputStream(file);
					try {
						indexer.index(stream);
					} finally {
						try {
							stream.close();
						} catch (Exception ignore) {
						}
					}
				} catch (Exception e) {
					log.error("Failed to index file " + file, e);
				}
			}
		}
		return Optional.of(indexer.complete());
	}

	private static Optional<IndexView> indexJar(File file, IndexFileFinder indexFileFinder) {
		File indexFile = indexFileFinder.findIndexFile(file);
		if (indexFile != null) {
			try {
				if (!indexFile.getParentFile().exists()) {
					indexFile.getParentFile().mkdirs();
				}
				if (indexFile.createNewFile()) {
					try {
						return Optional.of(JarIndexer.createJarIndex(file, new Indexer(), indexFile, false, false,
								false, System.out, System.err).getIndex());
					} catch (IOException e) {
						log.error("Failed to index '" + file + "'", e);
					}
				} else {
					try {
						return Optional.of(new IndexReader(new FileInputStream(indexFile)).read());
					} catch (IOException e) {
						log.error("Failed to read index file '" + indexFile + "'. Creating new index file.", e);
						if (indexFile.delete()) {
							return indexJar(file, indexFileFinder);
						} else {
							log.error("Failed to read index file '" + indexFile);
						}
					}
				}
			} catch (IOException e) {
				log.error("Unable to create index file '" + indexFile + "'", e);
			}
		} else {
			try {
				return Optional.of(JarIndexer
						.createJarIndex(file, new Indexer(), file.canWrite(), file.getParentFile().canWrite(), false)
						.getIndex());
			} catch (IOException e) {
				log.error("Failed to index '" + file + "'", e);
			}
		}
		return Optional.empty();
	}

	Tuple2<File, ClassInfo> getClassByName(DotName fqName) {
		// First look for type in the base index array
		return (baseIndex == null ? Stream.<Tuple2<File, ClassInfo>>empty()
				: Arrays.stream(
						baseIndex)
						.filter(
								jandexIndex -> jandexIndex != null)
						.map(jandexIndex -> jandexIndex.getClassByName(fqName))).filter(type -> type != null)
								.findFirst()
								// If not found look at indices owned by this
								// JandexIndex instance
								.orElseGet(() -> streamOfIndices()
										.map(e -> Tuples.of(e.getT1(), Optional.ofNullable(e.getT2().getClassByName(fqName))))
										.filter(t -> t.getT2().isPresent())
										.map(e -> Tuples.of(e.getT1(), e.getT2().get())).findFirst()
										.orElse(null));

	}

	private Optional<Tuple2<File, ClassInfo>> findMatch(DotName fqName) {
		return (baseIndex == null ? Stream.<Optional<Tuple2<File, ClassInfo>>>empty()
				: Arrays.stream(
						baseIndex)
						.filter(
								jandexIndex -> jandexIndex != null)
						.map(jandexIndex -> jandexIndex.findMatch(fqName))).filter(o -> o.isPresent())
								.findFirst()
								// If not found look at indices owned by this
								// JandexIndex instance
								.orElseGet(() -> streamOfIndices()
										.map(e -> {
											IndexView view = e.getT2();
											ClassInfo info = view.getClassByName(fqName);
											return Tuples.of(e.getT1(), Optional.ofNullable(info));
//											return Tuples.of(e.getT1(), Optional.ofNullable(e.getT2().getClassByName(fqName)));
										})
										.filter(t -> t.getT2().isPresent())
										.map(e -> Tuples.of(e.getT1(), e.getT2().get())).findFirst());
	}

	public Optional<File> findClasspathResourceForType(String fqName) {
		Optional<Tuple2<File, ClassInfo>> match = findMatch(DotName.createSimple(fqName));
		return Optional.ofNullable(match.isPresent() ? match.get().getT1() : null);
	}

	private Stream<Tuple2<File, IndexView>> streamOfIndices() {
		return index.entrySet().parallelStream().map(e -> Tuples.of(e.getKey(), e.getValue().get()))
				.filter(t -> t.getT2().isPresent()).map(t -> Tuples.of(t.getT1(), t.getT2().get()));
	}

	private Stream<Tuple3<String, File, ClassInfo>> getKnownTypesStream(File file) {
		Optional<IndexView> indexView = index.get(file).get();
		if (indexView.isPresent()) {
			return indexView.get().getKnownClasses().parallelStream()
					.map(info -> Tuples.of(info.name().toString(), file, info));
		}
		return Stream.empty();
	}

	private final Stream<String> getKnownPackages(File file) {
		Optional<IndexView> indexView = index.get(file).get();
		if (indexView.isPresent()) {
			return indexView.get().getKnownClasses().parallelStream().map(info -> {
				String name = info.name().toString();
				return name.substring(0, name.lastIndexOf('.'));
			}).distinct();
		}
		return Stream.empty();
	}

	Flux<Tuple3<File, ClassInfo, Double>> fuzzySearchTypes(String searchTerm) {
		Flux<Tuple3<File, ClassInfo, Double>> flux = Flux.fromIterable(knownTypes.values()).publishOn(Schedulers.parallel())
				.flatMap(s -> Flux.fromIterable(s.get()))
				.map(t -> Tuples.of(t.getT2(), t.getT3(), FuzzyMatcher.matchScore(searchTerm, t.getT1())))
				.filter(t -> t.getT3() != 0.0);
		if (baseIndex == null) {
			return flux;
		} else {
			return Flux.merge(flux,
					Flux.fromArray(baseIndex).flatMap(index -> index.fuzzySearchTypes(searchTerm)));
		}
	}

	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		Flux<Tuple2<String, Double>> flux = Flux.fromIterable(knownPackages.values()).publishOn(Schedulers.parallel())
				.flatMap(s -> Flux.fromIterable(s.get()))
				.map(pkg -> Tuples.of(pkg, FuzzyMatcher.matchScore(searchTerm, pkg))).filter(t -> t.getT2() != 0.0);
		if (baseIndex == null) {
			return flux;
		} else {
			return Flux.merge(flux, Flux.fromArray(baseIndex).flatMap(index -> index.fuzzySearchPackages(searchTerm)));
		}
	}

	Flux<Tuple2<File, ClassInfo>> allSubtypesOf(DotName name, boolean isInterface) {
		Flux<Tuple2<File, ClassInfo>> flux = Flux.fromIterable(index.keySet()).publishOn(Schedulers.parallel()).flatMap(file -> {
			Optional<IndexView> optional = index.get(file).get();
			if (optional.isPresent()) {
				return Flux
						.fromIterable(isInterface ? optional.get().getAllKnownImplementors(name)
								: optional.get().getAllKnownSubclasses(name))
						.publishOn(Schedulers.parallel()).map(info -> Tuples.of(file, info));
			} else {
				return Flux.empty();
			}
		});
		if (baseIndex == null) {
			return flux;
		} else {
			return Flux.merge(flux, Flux.fromArray(baseIndex).flatMap(index -> index.allSubtypesOf(name, isInterface)));
		}
	}
}
