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
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.springframework.ide.vscode.commons.java.IAnnotation;
import org.springframework.ide.vscode.commons.java.IField;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IMethod;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.IJavaProject.TypeFilter;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public class JandexIndex {
	
	@FunctionalInterface
	public static interface IndexFileFinder {
		File findIndexFile(File jarFile);
	}
	
	@FunctionalInterface
	public static interface JavadocProviderFactory {
		IJavadocProvider createJavadocProvider(File jarContainer);
	}
	
	private static final IJavadocProvider ABSENT_JAVADOC_PROVIDER = new IJavadocProvider() {
		
		@Override
		public IJavadoc getJavadoc(IType type) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IField field) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IMethod method) {
			return null;
		}

		@Override
		public IJavadoc getJavadoc(IAnnotation method) {
			return null;
		}
		
	};
	
	private Map<File, Supplier<Optional<IndexView>>> index;
	
	private JavadocProviderFactory javadocProviderFactory;
	
	private Map<File, Supplier<List<Tuple2<String, IType>>>> knownTypes;
	
	private Cache<File, IJavadocProvider> javadocProvidersCache = CacheBuilder.newBuilder().build();

	private JandexIndex[] baseIndex;
	
	public void setJvadocProviderFactory(JavadocProviderFactory sourceContainerProvider) {
		this.javadocProviderFactory = sourceContainerProvider;
	}
	
	public JavadocProviderFactory getJavadocProviderFactory() {
		return javadocProviderFactory;
	}
	
	public JandexIndex(Collection<File> classpathEntries, IndexFileFinder indexFileFinder, JavadocProviderFactory javadocProviderFactory, JandexIndex... baseIndex) {
		this.baseIndex = baseIndex;
		this.index = new ConcurrentHashMap<>();
		this.knownTypes = new HashMap<>();
		this.javadocProviderFactory = javadocProviderFactory;
		classpathEntries.forEach(file -> {
			index.put(file, Suppliers.memoize(() -> createIndex(file, indexFileFinder)));
			knownTypes.put(file, Suppliers.memoize(() -> getKnownTypesStream(file).collect(Collectors.toList())));
		});
	}
	
	private Optional<IndexView> createIndex(File file, IndexFileFinder indexFileFinder) {
		if (file.isFile() && file.getName().endsWith(".jar")) {
			return indexJar(file, indexFileFinder);
		} else if (file.isDirectory()) {
			return indexFolder(file);
		} else {
			return Optional.empty();
		}
	}
	
	private static Optional<IndexView> indexFolder(File folder) {
		Indexer indexer = new Indexer();
		for (Iterator<File> itr = com.google.common.io.Files.fileTreeTraverser().breadthFirstTraversal(folder).iterator(); itr.hasNext();) {
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
                	Log.log(e);
                }
			}
		}
		return Optional.of(indexer.complete());
	}
	
	private static Optional<IndexView> indexJar(File file, IndexFileFinder indexFileFinder) {
			File indexFile = indexFileFinder.findIndexFile(file);
			if (indexFile != null) {
				try {
					if (indexFile.createNewFile()) {
						try {
							return Optional.of(JarIndexer
									.createJarIndex(file, new Indexer(), indexFile,
											false, false, false, System.out, System.err)
									.getIndex());
						} catch (IOException e) {
							Log.log("Failed to index '" + file + "'", e);
						}
					} else {
						try {
							return Optional.of(new IndexReader(new FileInputStream(indexFile)).read());
						} catch (IOException e) {
							Log.log("Failed to read index file '" + indexFile + "'. Creating new index file.", e);
							if (indexFile.delete()) {
								return indexJar(file, indexFileFinder);
							} else {
								Log.log("Failed to read index file '" + indexFile);
							}
						}
					}
				} catch (IOException e) {
					Log.log("Unable to create index file '" + indexFile +"'");
				}
			} else {
				try {
					return Optional.of(JarIndexer
							.createJarIndex(file, new Indexer(), file.canWrite(), file.getParentFile().canWrite(), false)
							.getIndex());
				} catch (IOException e) {
					Log.log("Failed to index '" + file + "'", e);
				}
			}
		return Optional.empty();
	}

	public IType findType(String fqName) {
		return getClassByName(DotName.createSimple(fqName));
	}
	
	IType getClassByName(DotName fqName) {
		// First look for type in the base index array
		return (baseIndex == null ? Stream.<IType>empty()
				: Arrays.stream(baseIndex)
				.filter(jandexIndex -> jandexIndex != null)
				.map(jandexIndex -> jandexIndex.getClassByName(fqName)))
				.filter(type -> type != null)
				.findFirst()
				// If not found look at indices owned by this JandexIndex instance
				.orElseGet(() -> streamOfIndices()
									.map(e -> Tuples.of(e.getT1(), e.getT2().getClassByName(fqName)))
									.filter(e -> e.getT2() != null)
									.map(e -> createType(e))
									.findFirst()
									.orElse(null));
		
	}
	
	private IType createType(Tuple2<File, ClassInfo> match) {
		File classpathResource = match.getT1();
		IJavadocProvider javadocProvider = null;
		try {
			javadocProvider = javadocProvidersCache.get(classpathResource, () -> javadocProviderFactory == null ? ABSENT_JAVADOC_PROVIDER : javadocProviderFactory.createJavadocProvider(classpathResource));
		} catch (ExecutionException e) {
			Log.log(e);
		}
		return Wrappers.wrap(this, match.getT2(), javadocProvider); 
	}
	
	private Stream<Tuple2<File, IndexView>> streamOfIndices() {
		return index.entrySet().parallelStream().map(e -> Tuples.of(e.getKey(), e.getValue().get())).filter(t -> t.getT2().isPresent()).map(t -> Tuples.of(t.getT1(), t.getT2().get()));
	}
	
	private Stream<Tuple2<String, IType>> getKnownTypesStream(File file) {
		Optional<IndexView> indexView = index.get(file).get();
		if (indexView.isPresent()) {
			return indexView.get().getKnownClasses().parallelStream().map(info -> Tuples.of(info.name().toString(), createType(Tuples.of(file, info))));
		}
		return Stream.empty();
	}
	
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, TypeFilter typeFilter) {
		Flux<Tuple2<IType, Double>> flux = Flux.fromIterable(knownTypes.values())
				.publishOn(Schedulers.parallel())
				.flatMap(s -> Flux.fromIterable(s.get()))
				.filter(t -> typeFilter == null || typeFilter.accept(t.getT2()))
				.map(t -> Tuples.of(t.getT2(), FuzzyMatcher.matchScore(searchTerm, t.getT1())))
				.filter(t -> t.getT2() != 0.0);
		if (baseIndex == null) {
			return flux;
		} else {
			return Flux.merge(flux, Flux.fromArray(baseIndex).flatMap(index -> index.fuzzySearchTypes(searchTerm, typeFilter)));
		}
	}
	
	public Flux<IType> allSubtypesOf(IType type) {
		DotName name = DotName.createSimple(type.getFullyQualifiedName());
		Flux<IType> flux = Flux.fromIterable(index.keySet())
			.publishOn(Schedulers.parallel())
			.flatMap(file -> {
				Optional<IndexView> optional = index.get(file).get();
				if (optional.isPresent()) {
					return Flux.fromIterable(type.isInterface() ? optional.get().getAllKnownImplementors(name) : optional.get().getAllKnownSubclasses(name))
						.publishOn(Schedulers.parallel())
						.map(info -> createType(Tuples.of(file, info)));
				} else {
					return Flux.empty();
				}
			});
		if (baseIndex == null) {
			return flux;
		} else {
			return Flux.merge(flux, Flux.fromArray(baseIndex).flatMap(index -> index.allSubtypesOf(type)));
		}
	}
	
}
