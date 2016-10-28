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
package org.springframework.ide.vscode.project.harness;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.maven.java.classpathfile.JavaProjectWithClasspathFile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Test projects harness
 * 
 * @author Alex Boyko
 *
 */
public class ProjectsHarness {
	
	public static final ProjectsHarness INSTANCE = new ProjectsHarness();; 
	
	public Cache<String, IJavaProject> cache = CacheBuilder.newBuilder().build();
	
	private enum ProjectType {
		MAVEN,
		CLASSPATH_TXT
	}
	
	private ProjectsHarness() {
	}
	
	public IJavaProject project(ProjectType type, String name) throws Exception {
		return cache.get(type + "/" + name, () -> {
			Path testProjectPath = Paths.get(ProjectsHarness.class.getResource("/" + name).toURI());
			switch (type) {
			case MAVEN:
				return new MavenJavaProject(testProjectPath.resolve(MavenCore.POM_XML).toFile());
			case CLASSPATH_TXT:
				MavenCore.buildMavenProject(testProjectPath);
				return new JavaProjectWithClasspathFile(testProjectPath.resolve(MavenCore.CLASSPATH_TXT).toFile());
			default:
				throw new IllegalStateException("Bug!!! Missing case");
			}
		});
	}
	
	public MavenJavaProject mavenProject(String name) throws Exception {
		return (MavenJavaProject) project(ProjectType.MAVEN, name);
	}

	public IJavaProject javaProjectWithClasspathFile(String name) throws Exception {
		return project(ProjectType.CLASSPATH_TXT, name);
	}

}
