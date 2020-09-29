/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.test;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ExternalCommand;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ExternalProcess;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import junit.framework.TestCase;


public class NewSpringBootWizardTest extends TestCase {

	private BootProjectTestHarness harness;

	@Override
	protected void setUp() throws Exception {
		StsTestUtil.cleanUpProjects();
		harness = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	}

//	public void test_STS_4260() throws Exception {
//		if (OsUtils.isWindows()) {
//			return;
//		}
//		//This test / bug is about unix file permissions so skip it
//		// for windows.
//		IProject project = harness.createBootProject("simple-boot");
//		IFile mavenWrapper = project.getFile("mvnw");
//		assertTrue(mavenWrapper.exists());
//		assertExecutable(mavenWrapper);
//	}
//
//	public void testBuildshipProjectCreation() throws Exception {
//		IProject project = harness.createBootProject("simple-buildship",
//				withImportStrategy("GRADLE-Buildship 2.x")
//		);
//		assertTrue(project.hasNature(org.eclipse.buildship.core.configuration.GradleProjectNature.ID));
//		assertTrue(project.hasNature(SpringCore.NATURE_ID));
//	}
//
//	private void assertExecutable(IFile eclipseFile) throws Exception {
//		File file = eclipseFile.getRawLocation().toFile();
//		assertTrue("File does not exist: "+file, file.exists());
//		if (!file.canExecute()) {
//			ExternalCommand cmd = new ExternalCommand("ls", "-la", file.getParent());
//			ExternalProcess process = new ExternalProcess(new File("."), cmd, true);
//			fail("File is not executable: "+file+"\n"+process);
//		}
//	}
//
	public void testEchoJavaHome() throws Exception {
		ExternalCommand cmd = new ExternalCommand("echo", "$JAVA_HOME");
		File currentworkingFolder = new File(".").getCanonicalFile();
		System.out.println("JAVA_HOME=" + new ExternalProcess(currentworkingFolder, cmd, true).getErr());
		System.out.println("JAVA_HOME=" + new ExternalProcess(currentworkingFolder, cmd, true).getOut());
	}
}
