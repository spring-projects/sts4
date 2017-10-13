/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

/**
 * Tests covering Gradle project data
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectTest {
	
	private GradleJavaProject getGradleProject(String projectName) throws Exception {
		Path testProjectPath = Paths.get(GradleProjectTest.class.getResource("/" + projectName).toURI());
		return new GradleJavaProject(GradleCore.getDefault(), testProjectPath.toFile());
	}

	@Test
	public void testEclipseGradleProject() throws Exception {
		GradleJavaProject project = getGradleProject("empty-gradle-project");
		Set<Path> calculatedClassPath = project.getClasspath().getClasspathEntries().collect(Collectors.toSet());
		assertEquals(48, calculatedClassPath.size());
	}
	
	@Test
	public void outputFolder() throws Exception {
		GradleJavaProject project = getGradleProject("test-app-1");
		assertTrue(project.getClasspath().getOutputFolder().endsWith("bin"));
	}
	
	@Test
	public void gradleClasspathResource() throws Exception {
		GradleJavaProject project = getGradleProject("test-app-1");
		List<String> resources = project.getClasspath().getClasspathResources().collect(Collectors.toList());
		assertArrayEquals(new String[] {"test-resource-1.txt"}, resources.toArray(new String[resources.size()]));
	}
	
	@Test
	public void testGradleFileChanges() throws Exception {
		Path testProjectPath = Paths.get(GradleProjectTest.class.getResource("/empty-gradle-project").toURI());
		File gradleFile = testProjectPath.resolve(GradleCore.GRADLE_BUILD_FILE).toFile();
		BasicFileObserver fileObserver = new BasicFileObserver();
		GradleProjectCache manager = new GradleProjectCache(fileObserver, GradleCore.getDefault());
		IJavaProject[] projectChanged = new IJavaProject[] { null };
		IJavaProject[] projectDeleted = new IJavaProject[] { null };
		manager.addListener(new Listener() {
			@Override
			public void created(IJavaProject project) {}

			@Override
			public void changed(IJavaProject project) {
				projectChanged[0] = project;
			}
			@Override
			public void deleted(IJavaProject project) {
				projectDeleted[0] = project;
			}
		});
		
		// Get the project from cache
		GradleJavaProject cachedProject = manager.project(gradleFile);
		assertNotNull(cachedProject);
		
		fileObserver.notifyFileChanged(gradleFile.toURI().toString());
		assertEquals(cachedProject, projectChanged[0]);
		
		fileObserver.notifyFileDeleted(gradleFile.toURI().toString());
		assertEquals(cachedProject, projectDeleted[0]);
	}


}
