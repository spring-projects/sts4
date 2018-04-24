/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.function.Predicate;

import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Classpath for a Java artifact
 * 
 * @author Kris De Volder
 * @author Alex Boyko
 *
 */
public interface IClasspath {
	
	String getName();
	
	boolean exists();

	IType findType(String fqName);
	
	Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter);
	
	Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm);
	
	Flux<IType> allSubtypesOf(IType type);
	
	Path getOutputFolder();
	
	/**
	 * Classpath entries paths
	 * 
	 * @return collection of classpath entries in a form file/folder paths
	 * @throws Exception
	 */
	Collection<CPE> getClasspathEntries() throws Exception;

	/**
	 * Classpath resources paths relative to the source folder path
	 * @return classpath resource relative paths
	 */
	ImmutableList<String> getClasspathResources();
	
	ImmutableList<String> getSourceFolders();
	
	Optional<File> findClasspathResourceContainer(String fqName);
	
	ClasspathData createClasspathData() throws Exception;
	
	void reindex();
	
	Optional<URL> sourceContainer(File classpathResource);

	@Deprecated
	default Collection<Path> getClasspathEntryPaths() throws Exception {
		LinkedHashSet<Path> entries = new LinkedHashSet<>();
		for (CPE cpe : this.getClasspathEntries()) {
			if (Classpath.ENTRY_KIND_BINARY.equals(cpe.getKind())) {
				entries.add(Paths.get(cpe.getPath()));
			} else if (Classpath.ENTRY_KIND_SOURCE.equals(cpe.getKind())) {
				String of = cpe.getOutputFolder();
				if (of!=null) {
					entries.add(Paths.get(cpe.getOutputFolder()));
				} else {
					Path op = getOutputFolder();
					if (op!=null) {
						entries.add(op);
					}
				}
			}
		}
		return ImmutableList.copyOf(entries);
	}
}
