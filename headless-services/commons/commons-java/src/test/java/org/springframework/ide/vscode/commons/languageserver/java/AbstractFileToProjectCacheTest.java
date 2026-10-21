/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Patrick Baumgartner
 */
class AbstractFileToProjectCacheTest {

	@TempDir
	Path tempDir;

	@Test
	void removesOnlyProjectsWhoseBuildFilesAreUnderTheDeletedDirectory() {
		TestProjectCache cache = new TestProjectCache();
		File deletedBuildFile = tempDir.resolve("deleted-project/pom.xml").toFile();
		File retainedBuildFile = tempDir.resolve("retained-project/pom.xml").toFile();
		IJavaProject deletedProject = cache.project(deletedBuildFile);
		IJavaProject retainedProject = cache.project(retainedBuildFile);
		List<IJavaProject> deletedProjects = new ArrayList<>();
		cache.addListener(new ProjectObserver.Listener() {
			@Override
			public void created(IJavaProject project) {
			}

			@Override
			public void changed(IJavaProject project, boolean clean) {
			}

			@Override
			public void deleted(IJavaProject project) {
				deletedProjects.add(project);
			}
		});

		cache.removeProjectsUnder(tempDir.resolve("deleted-project"));

		assertEquals(List.of(retainedProject), List.copyOf(cache.all()));
		assertEquals(1, deletedProjects.size());
		assertSame(deletedProject, deletedProjects.get(0));
	}

	private static class TestProjectCache extends AbstractFileToProjectCache<IJavaProject> {

		TestProjectCache() {
			super(null, false, null);
		}

		@Override
		protected IJavaProject createProject(File key) {
			return mock(IJavaProject.class);
		}

		@Override
		protected void attachListeners(File file, IJavaProject project) {
		}

		@Override
		protected boolean update(IJavaProject project) {
			return false;
		}
	}
}
