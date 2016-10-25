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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.maven.MavenCore;

/**
 * Classpath for a maven project
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectClasspath implements IClasspath {

	private MavenCore maven;
	private MavenProject project;

	public MavenProjectClasspath(MavenProject project) {
		this(project, MavenCore.getInstance());
	}

	MavenProjectClasspath(MavenProject project, MavenCore maven) {
		this.maven = maven;
		this.project = project;
	}

	@Override
	public Collection<Path> getClasspathEntries() throws Exception {
		List<Path> entries = maven.resolveDependencies(project, null).stream().map(artifact -> {
			return Paths.get(artifact.getFile().toURI());
		}).collect(Collectors.toList());
		entries.add(Paths.get(new File(project.getBuild().getOutputDirectory()).toURI()));
		entries.add(Paths.get(new File(project.getBuild().getTestOutputDirectory()).toURI()));
		return entries;
	}

}
