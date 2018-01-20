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
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;

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
	ImmutableList<Path> getClasspathEntries() throws Exception;

	/**
	 * Classpath resources paths relative to the source folder path
	 * @return classpath resource relative paths
	 */
	ImmutableList<String> getClasspathResources();
	
	ImmutableList<String> getSourceFolders();
	
	Optional<File> findClasspathResourceContainer(String fqName);

}
