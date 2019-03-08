/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * Tests for comparing maven calculated dependencies with ours
 * 
 * @author Alex Boyko
 *
 */
public class DependencyTreeTest {
	
	private void testMavenClasspath(String projectName) throws Exception {
		Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
		MavenBuilder.newBuilder(testProjectPath).clean().pack().skipTests().execute();

		MavenProject project = MavenCore.getDefault().readProject(testProjectPath.resolve(MavenCore.POM_XML).toFile(), false);
		Set<Path> calculatedClassPath = MavenCore.getDefault().resolveDependencies(project, null).stream().map(artifact -> {
			return Paths.get(artifact.getFile().toURI());
		}).collect(Collectors.toSet());
		
		Set<Path> expectedClasspath = MavenCore.readClassPathFile(testProjectPath.resolve(MavenCore.CLASSPATH_TXT)).collect(Collectors.toSet());
		assertEquals(expectedClasspath, calculatedClassPath);		
	}
	
	@Test
	public void mavenClasspathTest_1() throws Exception {
		testMavenClasspath("empty-boot-project-with-classpath-file");
	}

	@Test
	public void mavenClasspathTest_2() throws Exception {
		testMavenClasspath("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
	}
	
	@Test
	public void dowloadDependenciesTest() throws Exception {
		String userSettingsFile = Paths.get(getClass().getResource("/maven-config/settings.xml").toURI()).toFile().toString();
		DefaultMavenConfiguration mavenConfig = new DefaultMavenConfiguration();
		mavenConfig.setUserSettingsFile(userSettingsFile);
		
		MavenCore maven = new MavenCore(mavenConfig);
		File localRepoFolder = maven.localRepositoryFolder();
		if (localRepoFolder.exists()) {
			deleteFolderAndContents(localRepoFolder.toPath());
		}
		
		assertFalse(localRepoFolder.exists());
		
		Path testProjectPath = Paths.get(getClass().getResource("/gs-rest-service-cors-boot-1.4.1-with-classpath-file").toURI());
		MavenProject project = maven.readProject(testProjectPath.resolve(MavenCore.POM_XML).toFile(), false);
		
		Set<Artifact> calculatedClassPath = maven.resolveDependencies(project, null);
		assertEquals(49, calculatedClassPath.size());
		
		String parentFolderPathStr = localRepoFolder.toString();
		for (Artifact artifact : calculatedClassPath) {
			assertTrue(artifact.isResolved());
			File file = artifact.getFile();
			assertNotNull(file);
			assertTrue(file.toString().startsWith(parentFolderPathStr));
			assertTrue(file.exists());
		}
		
		deleteFolderAndContents(localRepoFolder.toPath());
		assertFalse(localRepoFolder.exists());
	}
	
	@Test
	public void dowloadDependenciesWithProjectBuildingTest() throws Exception {
		String userSettingsFile = Paths.get(getClass().getResource("/maven-config/settings.xml").toURI()).toFile().toString();
		DefaultMavenConfiguration mavenConfig = new DefaultMavenConfiguration();
		mavenConfig.setUserSettingsFile(userSettingsFile);
		
		MavenCore maven = new MavenCore(mavenConfig);
		File localRepoFolder = maven.localRepositoryFolder();
		if (localRepoFolder.exists()) {
			deleteFolderAndContents(localRepoFolder.toPath());
		}
		
		assertFalse(localRepoFolder.exists());
		
		Path testProjectPath = Paths.get(getClass().getResource("/gs-rest-service-cors-boot-1.4.1-with-classpath-file").toURI());
		MavenProject project = maven.readProject(testProjectPath.resolve(MavenCore.POM_XML).toFile(), true);
		
		Set<Artifact> calculatedClassPath = project.getArtifacts();
		assertEquals(49, calculatedClassPath.size());
		
		String parentFolderPathStr = localRepoFolder.toString();
		for (Artifact artifact : calculatedClassPath) {
			assertTrue(artifact.isResolved());
			File file = artifact.getFile();
			assertNotNull(file);
			assertTrue(file.toString().startsWith(parentFolderPathStr));
			assertTrue(file.exists());
		}
		
		deleteFolderAndContents(localRepoFolder.toPath());
		assertFalse(localRepoFolder.exists());
	}
	
	private static void deleteFolderAndContents(Path folder) throws IOException {
		Files.walkFileTree(folder, new FileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}

		});
	}
	
}
