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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver.Listener;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectCache;
import org.springframework.ide.vscode.commons.util.BasicFileObserver;

/**
 * Tests for {@link MavenProjectCache}
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectManagerTest {

	@Test
	public void testPomFileChanges() throws Exception {
		Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/empty-boot-project-with-classpath-file").toURI());
		File pomFile = testProjectPath.resolve(MavenCore.POM_XML).toFile();
		BasicFileObserver fileObserver = new BasicFileObserver();
		MavenProjectCache cache = new MavenProjectCache(fileObserver, MavenCore.getDefault());
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
		
		fileObserver.notifyFileChanged(pomFile.toURI().toString());
		assertEquals(cachedProject, projectChanged[0]);
		
		fileObserver.notifyFileDeleted(pomFile.toURI().toString());
		assertEquals(cachedProject, projectDeleted[0]);
	}


}
