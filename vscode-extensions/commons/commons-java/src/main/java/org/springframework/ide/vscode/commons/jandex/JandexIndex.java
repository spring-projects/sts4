package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class JandexIndex {
	
	private static class Entry<K, V> {
		K key;
		V value;
		Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}
	
	@FunctionalInterface
	public static interface IndexFileFinder {
		File findIndexFile(File jarFile);
	}
	
	@FunctionalInterface
	public static interface JavadocProviderFactory {
		IJavadocProvider createJavadocProvider(File jarContainer);
	}
	
	private Supplier<List<Entry<File, IndexView>>> index;
	
	private JavadocProviderFactory javadocProviderFactory;
	
	private Cache<File, IJavadocProvider> javadocProvidersCache = CacheBuilder.newBuilder().build();
	
	public JandexIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder, JavadocProviderFactory javadocProviderFactory) {
		this(classpathEntries, indexFileFinder, Optional.empty(), javadocProviderFactory);
	}
	
	public void setJvadocProviderFactory(JavadocProviderFactory sourceContainerProvider) {
		this.javadocProviderFactory = sourceContainerProvider;
	}
	
	public JavadocProviderFactory getJavadocProviderFactory() {
		return javadocProviderFactory;
	}
	
	public JandexIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder, Optional<JandexIndex> baseIndex, JavadocProviderFactory javadocProviderFactory) {
		index = Suppliers.memoize(() -> {
			List<Entry<File, IndexView>> indices = buildIndex(classpathEntries, indexFileFinder).collect(Collectors.toList());
			if (baseIndex.isPresent()) {
				indices.addAll(baseIndex.get().index.get());
			}
			return indices;
		});
		this.javadocProviderFactory = javadocProviderFactory;
	}
	
	private Stream<Entry<File, IndexView>> buildIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder) {
		return classpathEntries
			.map(entry -> entry.toFile())
			.map(file -> {
					Optional<IndexView> index = Optional.empty();
					if (file.isFile() && file.getName().endsWith(".jar")) {
						index = indexJar(file, indexFileFinder);
					} else if (file.isDirectory()) {
						index = indexFolder(file);
					}
					return new Entry<>(file, index);
				})
			.filter(e -> e.value.isPresent())
			.map(e -> new Entry<>(e.key, e.value.get()));
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

	IType getClassByName(DotName className) {
		Optional<Entry<File, ClassInfo>> pair = index.get().stream().map(e -> new Entry<>(e.key, e.value.getClassByName(className))).filter(e -> e.value != null).findFirst();
		if (pair.isPresent()) {
			File classpathResource = pair.get().key;
			IJavadocProvider javadocProvider = null;
			try {
				javadocProvider = javadocProvidersCache.get(pair.get().key, () -> javadocProviderFactory == null ? null : javadocProviderFactory.createJavadocProvider(classpathResource));
			} catch (ExecutionException e) {
				Log.log(e);
			}
			return Wrappers.wrap(this, pair.get().value, javadocProvider); 
		} else {
			return null;
		}
	}
	
}
