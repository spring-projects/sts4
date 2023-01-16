/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.junit.rules.TemporaryFolder;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.frameworks.test.util.Asserter;

public class TestUtils {

	static IProject createTestProject(String name, File testProjectLocation) throws Exception {
		File testProjectSourceLocation = new File(FileLocator.toFileURL(Platform.getBundle("org.springframework.tooling.jdt.ls.commons.test").getEntry("test-projects/"+name)).toURI());

		FileUtils.copyDirectory(testProjectSourceLocation, testProjectLocation);

		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(null);
		desc.setName(name);
		desc.setLocation(Path.fromOSString(testProjectLocation.toString()));
		project.create(desc, null);
		project.open(null);

		assertTrue(project.hasNature(JavaCore.NATURE_ID));
		return project;
	}
	
	static IProject createTestProject(String name, TemporaryFolder tmp) throws Exception {
		File testProjectLocation = tmp.newFolder(name);
		return TestUtils.createTestProject(name, testProjectLocation);
	}
	
	static void deleteAllProjects() throws Exception {
		CompletableFuture<Void> done = new CompletableFuture<Void>();
		WorkspaceJob job = new WorkspaceJob("Delete projects") {
			@Override public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
				try {
					ACondition.waitFor("Deleting all projects", Duration.ofMinutes(1), () -> {
						ResourcesPlugin.getWorkspace().getRuleFactory().buildRule();
						IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
						for (IProject project : allProjects) {
							project.refreshLocal(IResource.DEPTH_INFINITE, null);
							safe(() -> project.close(null));
							project.delete(false, true, new NullProgressMonitor());
							assertFalse(project.exists());
						}
					});
					done.complete(null);
				} catch (Throwable e) {
					done.completeExceptionally(e);
				}
				return Status.OK_STATUS;
			}

		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
		done.get();
	}
	
	static void safe(Asserter doit) {
		try {
			doit.execute();
		} catch (Throwable e) {
			System.err.println("Ignore exception: "+e.getMessage());
		}
	}

}
