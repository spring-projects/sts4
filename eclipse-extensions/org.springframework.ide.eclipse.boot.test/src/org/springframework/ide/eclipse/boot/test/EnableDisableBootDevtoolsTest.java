/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import static org.junit.Assert.fail;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.bootVersionAtLeast;
import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withStarters;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.core.IMavenCoordinates;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SpringBootCore;
import org.springframework.ide.eclipse.boot.core.SpringBootStarters;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.ui.EnableDisableBootDevtools;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

public class EnableDisableBootDevtoolsTest {

	//TODO: better test coverage for EnableDisableBootDevtools

	private static final long MAVEN_POM_REFRESH_TIMEOUT = 3*60*1000;

	/**
	 * We want the devtools enablement action not to depend on initializr service.
	 * So lets explicitly mock it with a 'unavalailable' service.
	 */
	private InitializrService initializr = new InitializrService() {
		@Override
		public SpringBootStarters getStarters(String bootVersion) {
			return null;
		}

		@Override
		public String getPom(Map<String, ?> parameters) throws Exception {
			throw new UnsupportedOperationException();
		}
	};
	private SpringBootCore springBootCore = new SpringBootCore(initializr);
	private BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	private EnableDisableBootDevtools handler = new EnableDisableBootDevtools(springBootCore);

	@Rule
	public AutobuildingEnablement autobuilding = new AutobuildingEnablement(true);

	@Before
	public void setUp() throws Exception {
		StsTestUtil.deleteAllProjects();
	}

	@Test
	public void addBootDevtools() throws Exception {
		final ISpringBootProject project = springBootCore.project(
				projects.createBootWebProject("dev-starters-tester", bootVersionAtLeast("1.3.0")));

		StructuredSelection selection = new StructuredSelection(new IProject[] {project.getProject() });

		assertNoDependency(project.getDependencies(), BootPropertyTester.SPRING_BOOT_DEVTOOLS_GID, BootPropertyTester.SPRING_BOOT_DEVTOOLS_AID);

		handler.execute(selection, null);

		ACondition.waitFor("Wait for 'devtools' starter to be added", MAVEN_POM_REFRESH_TIMEOUT, () -> {
			assertDependency(project.getDependencies(), BootPropertyTester.SPRING_BOOT_DEVTOOLS_GID, BootPropertyTester.SPRING_BOOT_DEVTOOLS_AID);
		});

	}

	@Test
	public void removeBootDevtools() throws Exception {
		final ISpringBootProject project = springBootCore.project(
				projects.createBootWebProject("dev-starters-tester",
						bootVersionAtLeast("1.3.0"),
						withStarters("devtools")
				)
		);
		StructuredSelection selection = new StructuredSelection(new IProject[] {project.getProject() });

		assertDependency(project.getDependencies(), BootPropertyTester.SPRING_BOOT_DEVTOOLS_GID, BootPropertyTester.SPRING_BOOT_DEVTOOLS_AID);

		handler.execute(selection, null);

		ACondition.waitFor("Wait for 'devtools' starter to be removed", MAVEN_POM_REFRESH_TIMEOUT, () -> {
			assertNoDependency(project.getDependencies(), BootPropertyTester.SPRING_BOOT_DEVTOOLS_GID, BootPropertyTester.SPRING_BOOT_DEVTOOLS_AID);
		});
	}

	///////////////////////////////////////////////////////////////////////////////

	private void assertDependency(List<IMavenCoordinates> dependencies, String gid, String aid) {
		for (IMavenCoordinates d : dependencies) {
			if (gid.equals(d.getGroupId()) && aid.equals(d.getArtifactId())) {
				return; //okay!
			}
		}
		fail("No dependency "+gid+":"+aid+" in "+dependencies);
	}

	private void assertNoDependency(List<IMavenCoordinates> dependencies, String gid, String aid) {
		for (IMavenCoordinates d : dependencies) {
			if (gid.equals(d.getGroupId()) && aid.equals(d.getArtifactId())) {
				fail("Dependency found but not expected "+gid+":"+aid+" in "+dependencies);
			}
		}
	}
}
