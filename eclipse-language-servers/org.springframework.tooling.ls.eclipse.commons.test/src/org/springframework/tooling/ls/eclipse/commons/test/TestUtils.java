/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
 package org.springframework.tooling.ls.eclipse.commons.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class TestUtils {
	
	public static IJavaProject createTestProject(String name) throws Exception {
		File testProjectSourceLocation = new File(FileLocator.toFileURL(Platform.getBundle("org.springframework.tooling.ls.eclipse.commons.test").getEntry("test-projects/"+name)).toURI());
		File targetWorkspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
		
		FileUtils.copyDirectory(testProjectSourceLocation, new File(targetWorkspace, name));

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		project.create(null);
		project.open(null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);

		assertTrue(project.hasNature(JavaCore.NATURE_ID));
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

		return JavaCore.create(project);
	}
	
	public static void deleteAllProjects() throws Exception {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : allProjects) {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
			project.close(null);
			deleteProject(project);
		}
		ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}
	
	private static void deleteProject(IProject project) throws Exception {
		int retryCount = 10; // wait 1 minute at most
		Exception lastException = null;
		while (project.exists() && --retryCount >= 0) {
			try {
				project.delete(true, true, new NullProgressMonitor());
				lastException = null;
			} catch (Exception e) {
				lastException = e;
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e1) {
				}
			}
		}
		if (lastException!=null) {
			throw lastException;
		}
	}


}
