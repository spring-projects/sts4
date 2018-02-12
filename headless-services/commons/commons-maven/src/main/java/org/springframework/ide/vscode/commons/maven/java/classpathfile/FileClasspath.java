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
package org.springframework.ide.vscode.commons.maven.java.classpathfile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.maven.MavenCore;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Classpath for a project containing classpath text file
 * 
 * @author Alex Boyko
 *
 */
public class FileClasspath implements IClasspath {

	private Path classpathFilePath;

	public FileClasspath(Path classpathFilePath) {
		this.classpathFilePath = classpathFilePath;
	}

	@Override
	public ImmutableList<Path> getClasspathEntries() throws Exception {
		return ImmutableList.copyOf(Stream.concat(MavenCore.readClassPathFile(classpathFilePath),
				Stream.of(classpathFilePath.getParent().resolve("target/classes"),
						classpathFilePath.getParent().resolve("target/test-classes"))).collect(Collectors.toList()));
	}

	@Override
	public ImmutableList<String> getClasspathResources() {
		return ImmutableList.of();
	}
	
	@Override
	public String getName() {
		return classpathFilePath.toFile().getParentFile().getName();
	}

	@Override
	public boolean exists() {
		return Files.exists(classpathFilePath);
	}

	@Override
	public IType findType(String fqName) {
		//TODO: implement
		return null;
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, Predicate<IType> typeFilter) {
		return Flux.empty();
	}

	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		return Flux.empty();
	}

	@Override
	public Flux<IType> allSubtypesOf(IType type) {
		return Flux.empty();
	}

	@Override
	public Path getOutputFolder() {
		return null;
	}

	@Override
	public ImmutableList<String> getSourceFolders() {
		return ImmutableList.of();
	}

	@Override
	public Optional<File> findClasspathResourceContainer(String fqName) {
		return Optional.empty();
	}

	@Override
	public ClasspathData createClasspathData() throws Exception {
		return ClasspathData.from(getName(), getClasspathEntries(), getClasspathResources(), getOutputFolder());
	}

	@Override
	public void reindex() {
	}
}
