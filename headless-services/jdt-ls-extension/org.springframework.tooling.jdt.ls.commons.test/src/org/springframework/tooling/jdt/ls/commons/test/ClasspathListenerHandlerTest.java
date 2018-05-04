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
package org.springframework.tooling.jdt.ls.commons.test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
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
import org.junit.After;
import org.junit.Test;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;
import org.springframework.tooling.jdt.ls.commons.classpath.ClientCommandExecutor;
import org.springframework.tooling.jdt.ls.commons.classpath.ReusableClasspathListenerHandler;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import junit.framework.AssertionFailedError;

public class ClasspathListenerHandlerTest {
	
	private MockClasspathCache classpaths = new MockClasspathCache();
	private ReusableClasspathListenerHandler service = new ReusableClasspathListenerHandler(classpaths);
	
	@Test public void classpathIsSentForExistingProject() throws Exception {
		String projectName = "classpath-test-simple-java-project";
		IProject project = createTestProject(projectName);
		File loc = project.getLocation().toFile();
		
		service.addClasspathListener(classpaths.commandId);
		ACondition.waitFor("Project with classpath to appear", Duration.ofSeconds(50), () -> {
			Classpath cp = classpaths.getFor(loc).classpath;
			assertTrue(cp.getEntries().stream().filter(cpe -> Classpath.isSource(cpe)).count()==1); //has 1 source entry
			assertClasspath(cp, cp.getEntries().stream().filter(cpe -> Classpath.isBinary(cpe) && cpe.isSystem()).count()>=1); //has some system libraries
		});
	}


	@Test public void classpathIsSentForNewProject_and_removedForDeletedProject() throws Exception {
		service.addClasspathListener(classpaths.commandId);
		
		String projectName = "classpath-test-simple-java-project";
		IProject project = createTestProject(projectName);
		File loc = project.getLocation().toFile();
		ACondition.waitFor("Project with classpath to appear", Duration.ofSeconds(5), () -> {
			Classpath cp = classpaths.getFor(loc).classpath;
			assertTrue(cp.getEntries().stream().filter(cpe -> Classpath.isSource(cpe)).count()==1); //has 1 source entry
			assertClasspath(cp, cp.getEntries().stream().filter(cpe -> Classpath.isBinary(cpe) && cpe.isSystem()).count()>=1); //has some system libraries
		});
		Logger.log("=== Deleteing project");
		project.delete(false, true, null);
		ACondition.waitFor("Project classpath to disapear", Duration.ofSeconds(5), () -> {
			Info cp = classpaths.getFor(loc);
			assertNull(cp);
		});
		
	}

	///////////// harness stuff below ///////////////////////////////////////////////
	
	static class Info {
		public final String name;
		public final Classpath classpath;
		
		private Info(String name, Classpath cp) {
			super();
			this.name = name;
			this.classpath = cp;
		}
	}

	public class MockClasspathCache implements ClientCommandExecutor {

		String commandId = RandomStringUtils.randomAlphabetic(8);
		
		Map<File, Info> classpaths = new HashMap<>();
		
		@Override
		public synchronized Object executeClientCommand(String id, Object... params) throws Exception {
			if (id.equals(commandId)) {
				System.out.println("received: "+Arrays.asList(params));
				File projectLoc = new File(new URI((String) params[0]));
				String name = (String) params[1];
				boolean deleted = (boolean) params[2];
				if (deleted) {
					System.out.println("DELETING "+name);
					classpaths.remove(projectLoc);
				} else {
					Classpath cp = (Classpath) params[3];
					System.out.println("PUT "+name+" "+cp.getEntries().size()+" entries");
					classpaths.put(projectLoc, new Info(name, cp));
				}
			}
			return "whatever";
		}

		public synchronized Info getFor(File location) {
			return classpaths.get(location);
		}

		public void dispose() throws Exception {
			service.removeClasspathListener(commandId);
			deleteAllProjects();
		}

	}
	
	@After
	public void tearDown() throws Exception {
		classpaths.dispose();
		assertTrue(service.hasNoActiveSubscriptions());
	}
	
	private static void assertClasspath(Classpath cp, boolean b) {
		if (!b) {
			StringBuilder buf = new StringBuilder();
			for (CPE cpe : cp.getEntries()) {
				buf.append("\n   ");
				buf.append(cpe);
			}
			throw new AssertionFailedError("Unexpected classpath:" + buf);
		}
	}

	private IProject createTestProject(String name) throws Exception {
		File testProjectLocation = new File(FileLocator.toFileURL(Platform.getBundle("org.springframework.tooling.jdt.ls.commons.test").getEntry("test-projects/"+name)).toURI());
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription(null);
		desc.setName(name);
		desc.setLocation(Path.fromOSString(testProjectLocation.toString()));
		project.create(desc, null);
		project.open(null);
		
		assertTrue(project.hasNature(JavaCore.NATURE_ID));
		return project;
	}	
	

	public static void deleteAllProjects() throws Exception {
		CompletableFuture<Void> done = new CompletableFuture<Void>();
		WorkspaceJob job = new WorkspaceJob("Delete projects") {
			@Override public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {
				try {
					ResourcesPlugin.getWorkspace().getRuleFactory().buildRule();
					IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					for (IProject project : allProjects) {
						project.refreshLocal(IResource.DEPTH_INFINITE, null);
						project.close(null);
						project.delete(false, true, new NullProgressMonitor());
					}
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
}
