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
package org.springframework.tooling.jdt.ls.commons;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.junit.After;
import org.junit.Test;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath;
import org.springframework.tooling.jdt.ls.commons.classpath.ClientCommandExecutor;
import org.springframework.tooling.jdt.ls.commons.classpath.ReusableClasspathListenerHandler;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

public class ClasspathListenerHandlerTest {

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
					classpaths.remove(projectLoc);
				} else {
					Classpath cp = (Classpath) params[3];
					classpaths.put(projectLoc, new Info(name, cp));
				}
			}
			return "whatever";
		}

		public synchronized Info getFor(IProject project) {
			File location = project.getLocation().toFile();
			return classpaths.get(location);
		}

		public void dispose() {
			service.removeClasspathListener(commandId);
		}

	}
	private MockClasspathCache classpaths = new MockClasspathCache();
	private ReusableClasspathListenerHandler service = new ReusableClasspathListenerHandler(classpaths);
	
	@After
	public void tearDown() {
		classpaths.dispose();
		assertTrue(service.hasNoActiveSubscriptions());
	}
	
	@Test
	public void classpathIsSentForExistingProject() throws Exception {
		String projectName = "classpath-test-simple-java-project";
		IProject project = createTestProject(projectName);
		
		service.addClasspathListener(classpaths.commandId);
		ACondition.waitFor("Project with classpath to appear", Duration.ofSeconds(5), () -> {
			Classpath cp = classpaths.getFor(project).classpath;
			assertTrue(cp.getEntries().stream().filter(cpe -> Classpath.isSource(cpe)).count()==1); //has 1 source entry
			assertTrue(cp.getEntries().stream().filter(cpe -> Classpath.isBinary(cpe) && cpe.isSystem()).count()>=1); //has some system libraries
		});
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
	
	
}
