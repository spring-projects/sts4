package org.springframework.ide.vscode.commons.jandex;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jboss.jandex.CompositeIndex;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexReader;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;
import org.jboss.jandex.JarIndexer;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class JandexIndex {
	
	@FunctionalInterface
	public static interface IndexFileFinder {
		File findIndexFile(File jarFile);
	}
	
	private Supplier<IndexView> index;
	
	public JandexIndex(Stream<Path> classpathEntries) {
		this(classpathEntries, jarFile -> null, Optional.empty());
	}
	
	public JandexIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder) {
		this(classpathEntries, indexFileFinder, Optional.empty());
	}
	
	public JandexIndex(Stream<Path> classpathEntries, Optional<JandexIndex> baseIndex) {
		this(classpathEntries, jarFile -> null, baseIndex);
	}
	
	public JandexIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder, Optional<JandexIndex> baseIndex) {
		index = Suppliers.memoize(() -> {
			if (baseIndex.isPresent()) {
				return CompositeIndex.create(baseIndex.get().index.get(), buildIndex(classpathEntries, indexFileFinder));
			} else {
				return buildIndex(classpathEntries, indexFileFinder);
			}
		});
	}
	
	private static CompositeIndex buildIndex(Stream<Path> classpathEntries, IndexFileFinder indexFileFinder) {
		return CompositeIndex.create(classpathEntries
			.map(entry -> entry.toFile())
			.map(file -> {
					if (file.isFile() && file.getName().endsWith(".jar")) {
						return indexJar(file, indexFileFinder);
					} else if (file.isDirectory()) {
						return indexFolder(file);
					} else {
						return Optional.<IndexView>empty();
					}
				})
			.filter(o -> o.isPresent())
			.map(o -> o.get())
			.collect(Collectors.toList()));
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
		IndexView compositeIndex = index.get();
		return Wrappers.wrap(compositeIndex, compositeIndex.getClassByName(DotName.createSimple(fqName)));
	}
	
}
