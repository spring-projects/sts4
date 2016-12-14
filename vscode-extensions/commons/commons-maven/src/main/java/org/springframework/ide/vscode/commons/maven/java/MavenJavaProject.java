/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.project.MavenProject;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.javadoc.IJavadoc;
import org.springframework.ide.vscode.commons.maven.MavenCore;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Wrapper for Maven Core project
 * 
 * @author Alex Boyko
 *
 */
public class MavenJavaProject implements IJavaProject {

	private MavenProjectClasspath classpath;
	private MavenCore maven;

	public MavenJavaProject(File pom) throws Exception {
		this.maven = MavenCore.getDefault();
		this.classpath = new MavenProjectClasspath(pom, maven);
	}

	@Override
	public String getElementName() {
		return classpath.getName();
	}

	@Override
	public IJavadoc getJavaDoc() {
		return null;
	}

	@Override
	public boolean exists() {
		return classpath.exists();
	}

	@Override
	public IType findType(String fqName) {
		return classpath.findType(fqName);
	}

	@Override
	public Flux<Tuple2<IType, Double>> fuzzySearchTypes(String searchTerm, TypeFilter typeFilter) {
		return classpath.fuzzySearchType(searchTerm, typeFilter);
	}
	
	@Override
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		return classpath.fuzzySearchPackages(searchTerm);
	}

	@Override
	public Flux<IType> allSubtypesOf(IType type) {
		return classpath.allSubtypesOf(type);
	}
		
	@Override
	public MavenProjectClasspath getClasspath() {
		return classpath;
	}

	public Path getOutputFolder() {
		return Paths.get(new File(classpath.getOutputFolder()).toURI());
	}

}
