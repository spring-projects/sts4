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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.assertj.core.util.Files;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.Sts4LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

import com.google.common.collect.ImmutableList;

/**
 * Tests covering Gradle project data
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectTest {
	
	private Sts4LanguageServer server;
	private BasicFileObserver fileObserver;
	
	@Before
	public void setup() throws Exception {
		fileObserver = new BasicFileObserver();
		server = mock(Sts4LanguageServer.class);
		SimpleWorkspaceService workspaceService = mock(SimpleWorkspaceService.class);
		when(workspaceService.getFileObserver()).thenReturn(fileObserver);
		when(server.getWorkspaceService()).thenReturn(workspaceService);
	}
	
	private static void writeContent(File file, String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(content);
		} finally {
			writer.close();
		}
	}
	
	private GradleJavaProject getGradleProject(String projectName) throws Exception {
		Path testProjectPath = Paths.get(GradleProjectTest.class.getResource("/" + projectName).toURI());
		return new GradleJavaProject(GradleCore.getDefault(), testProjectPath.toFile());
	}

	@Test
	public void testEclipseGradleProject() throws Exception {
		GradleJavaProject project = getGradleProject("empty-gradle-project");
		ImmutableList<Path> calculatedClassPath = project.getClasspath().getClasspathEntries();
		assertEquals(48, calculatedClassPath.size());
	}
	
	@Test
	public void outputFolder() throws Exception {
		GradleJavaProject project = getGradleProject("test-app-1");
		assertTrue(project.getClasspath().getOutputFolder().toString().contains("/bin"));
	}
	
	@Test
	public void gradleClasspathResource() throws Exception {
		GradleJavaProject project = getGradleProject("test-app-1");
		List<String> resources = project.getClasspath().getClasspathResources();
		assertArrayEquals(new String[] {"test-resource-1.txt"}, resources.toArray(new String[resources.size()]));
	}
	
	@Test
	public void testGradleFileChanges() throws Exception {
		Path testProjectPath = Paths.get(GradleProjectTest.class.getResource("/empty-gradle-project").toURI());
		File gradleFile = testProjectPath.resolve(GradleCore.GRADLE_BUILD_FILE).toFile();
		
		String gradelFileContents = Files.contentOf(gradleFile, Charset.defaultCharset());
		
		try {
			GradleProjectCache manager = new GradleProjectCache(server, GradleCore.getDefault(), false, null);
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
			
			ImmutableList<Path> calculatedClassPath = cachedProject.getClasspath().getClasspathEntries();
			assertEquals(48, calculatedClassPath.size());
			
			fileObserver.notifyFileChanged(gradleFile.toURI().toString());
			assertNull(projectChanged[0]);
			
			writeContent(gradleFile, Files.contentOf(testProjectPath.resolve("build.newgradle").toFile(), Charset.defaultCharset()));
			fileObserver.notifyFileChanged(gradleFile.toURI().toString());
			assertNotNull(projectChanged[0]);
			assertEquals(cachedProject, projectChanged[0]);
			calculatedClassPath = cachedProject.getClasspath().getClasspathEntries();
			assertEquals(49, calculatedClassPath.size());
	
			
			fileObserver.notifyFileDeleted(gradleFile.toURI().toString());
			assertEquals(cachedProject, projectDeleted[0]);
		} finally {
			writeContent(gradleFile, gradelFileContents);
		}
	}

	@Test
	public void findGradleProjectWithStandardBuildFile() throws Exception {
		GradleProjectFinder finder = new GradleProjectFinder(new GradleProjectCache(server, GradleCore.getDefault(), false, null));
		File sourceFile = new File(GradleProjectTest.class.getResource("/test-app-1/src/main/java/Library.java").toURI());
		Optional<IJavaProject> project = finder.find(sourceFile);
		assertTrue(project.isPresent());
		assertTrue(project.get() instanceof GradleJavaProject);
		GradleJavaProject gradleProject = (GradleJavaProject) project.get();
		assertEquals(new File(GradleProjectTest.class.getResource("/test-app-1").toURI()), gradleProject.getLocation());
	}

	@Test
	public void findGradleProjectWithNonStandardBuildFile() throws Exception {
		GradleProjectFinder finder = new GradleProjectFinder(new GradleProjectCache(server, GradleCore.getDefault(), false, null));
		File sourceFile = new File(GradleProjectTest.class.getResource("/test-app-2/src/main/java/Library.java").toURI());
		Optional<IJavaProject> project = finder.find(sourceFile);
		assertTrue(project.isPresent());
		assertTrue(project.get() instanceof GradleJavaProject);
		GradleJavaProject gradleProject = (GradleJavaProject) project.get();
		assertEquals(new File(GradleProjectTest.class.getResource("/test-app-2").toURI()), gradleProject.getLocation());
	}
	
}
