/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.assertj.core.util.Files;
import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectCache;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

import com.google.common.collect.ImmutableList;

/**
 * Tests for {@link MavenProjectCache}
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectManagerTest {
	
	private static void writeContent(File file, String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(file);
			writer.write(content);
		} finally {
			writer.close();
		}
	}
	
	@Test
	public void testPomFileChanges() throws Exception {
		Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/empty-boot-project-with-classpath-file").toURI());
		File pomFile = testProjectPath.resolve(MavenCore.POM_XML).toFile();
		
		String pomFileContents = Files.contentOf(pomFile, Charset.defaultCharset());

		try {
			BasicFileObserver fileObserver = new BasicFileObserver();
			MavenProjectCache cache = new MavenProjectCache(fileObserver, MavenCore.getDefault(), false, null);
			IJavaProject[] projectChanged = new IJavaProject[] { null };
			IJavaProject[] projectDeleted = new IJavaProject[] { null };
			cache.addListener(new Listener() {
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
			MavenJavaProject cachedProject = cache.project(pomFile);
			assertNotNull(cachedProject);
			
			ImmutableList<Path> calculatedClassPath = cachedProject.getClasspath().getClasspathEntries();
			assertEquals(48, calculatedClassPath.size());
			
			fileObserver.notifyFileChanged(pomFile.toURI().toString());
			assertNull(projectChanged[0]);
			
			writeContent(pomFile, Files.contentOf(testProjectPath.resolve("pom.newxml").toFile(), Charset.defaultCharset()));
			fileObserver.notifyFileChanged(pomFile.toURI().toString());
			assertNotNull(projectChanged[0]);
			assertEquals(cachedProject, projectChanged[0]);
			calculatedClassPath = cachedProject.getClasspath().getClasspathEntries();
			assertEquals(49, calculatedClassPath.size());
				
			fileObserver.notifyFileDeleted(pomFile.toURI().toString());
			assertEquals(cachedProject, projectDeleted[0]);
		} finally {
			//restore original content
			writeContent(pomFile, pomFileContents);
		}
	}


}
