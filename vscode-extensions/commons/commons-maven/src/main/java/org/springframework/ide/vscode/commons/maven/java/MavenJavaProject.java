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
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.project.MavenProject;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.HtmlSnippet;

/**
 * Wrapper for Maven Core project
 * 
 * @author Alex Boyko
 *
 */
public class MavenJavaProject implements IJavaProject {

	private MavenProject mavenProject;
	private MavenProjectClasspath classpath;
	private MavenCore maven;

	public MavenJavaProject(File pom) throws Exception {
		this.maven = MavenCore.getInstance();
		this.mavenProject = maven.readProject(pom);
		this.classpath = new MavenProjectClasspath(mavenProject, maven);
	}

	@Override
	public String getElementName() {
		return mavenProject.getName();
	}

	@Override
	public HtmlSnippet getJavaDoc() {
		return null;
	}

	@Override
	public boolean exists() {
		return mavenProject != null;
	}

	@Override
	public IType findType(String fqName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IClasspath getClasspath() {
		return classpath;
	}

	public Path getOutputFolder() {
		return Paths.get(URI.create(mavenProject.getBuild().getOutputDirectory()));
	}
		
}
