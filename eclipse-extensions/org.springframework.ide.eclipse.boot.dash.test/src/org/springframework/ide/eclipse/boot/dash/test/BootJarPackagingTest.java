/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.springsource.ide.eclipse.commons.tests.util.StsTestUtil.assertNoErrors;

import java.io.File;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.CloudApplicationArchiverStrategies;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.CloudApplicationArchiverStrategy;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.ICloudApplicationArchiver;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.JavaUtils;
import org.springframework.ide.eclipse.boot.test.util.LaunchResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.setPackage;

public class BootJarPackagingTest extends StsTestCase {

	private JavaUtils java = new JavaUtils();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		StsTestUtil.deleteAllProjects();
		StsTestUtil.setAutoBuilding(false);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testSimple() throws Exception {
		UserInteractions ui = Mockito.mock(UserInteractions.class);
		BootProjectTestHarness harness = getHarness();
		IProject project = harness.createBootProject("simple-boot",
				setPackage("demo")
		);
		createFile(project, "src/main/java/demo/Greeter.java",
				"package demo;\n" +
				"\n" +
				"import org.springframework.boot.CommandLineRunner;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class Greeter implements CommandLineRunner {\n" +
				"\n" +
				"	@Override\n" +
				"	public void run(String... arg0) throws Exception {\n" +
				"		System.out.println(\"Hello World!\");\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);
		StsTestUtil.assertNoErrors(project); // Builds the project
		File jarFile = packageAsJar(project, ui);
		LaunchResult result = java.runJar(jarFile);
		assertContains("Hello World!", result.out);
		assertEquals(0, result.terminationCode);
		Mockito.verifyZeroInteractions(ui);
	}

	public void testProjectDependency() throws Exception {
		IProject depProject = createPredefinedProject("demo-lib");
		StsTestUtil.assertNoErrors(depProject);
		UserInteractions ui = Mockito.mock(UserInteractions.class);
		BootProjectTestHarness harness = getHarness();

		IProject project = harness.createBootProject("simple-boot",
				setPackage("demo")
		);
		fileReplace(project, "pom.xml",
				"</dependencies>",
				"	<dependency>\n" +
				"			<groupId>org.demo</groupId>\n" +
				"			<artifactId>demo-lib</artifactId>\n" +
				"			<version>0.0.1</version>\n" +
				"		</dependency>\n" +
				"	</dependencies>");


		createFile(project, "src/main/java/demo/Greeter.java",
				"package demo;\n" +
				"\n" +
				"import org.demo.lib.TheLib;\n" +
				"import org.springframework.boot.CommandLineRunner;\n" +
				"import org.springframework.stereotype.Component;\n" +
				"\n" +
				"@Component\n" +
				"public class Greeter implements CommandLineRunner {\n" +
				"\n" +
				"	@Override\n" +
				"	public void run(String... arg0) throws Exception {\n" +
				"		System.out.println(TheLib.createGreeting(\"Kris\"));\n" +
				"	}\n" +
				"\n" +
				"}\n"
		);
		assertNoErrors(project);

		File jarFile = packageAsJar(project, ui);
		assertEntries(jarFile,
				"lib/demo-lib.jar"
		);
		LaunchResult result = java.runJar(jarFile);
		assertContains("Hello, Kris!", result.out);
		assertEquals(0, result.terminationCode);

		Mockito.verifyZeroInteractions(ui);
	}

	/**
	 * Verifies that jarFile has at least a given list of entries (in any order).
	 */
	private void assertEntries(File file, String... expected) throws Exception {
		JarFile jarFile = new JarFile(file);
		try {
			for (String name : expected) {
				assertNotNull("Missing expected Jar Entry: "+name, jarFile.getJarEntry(name));
			}
		} finally {
			jarFile.close();
		}
	}

	public static File packageAsJar(IProject project, UserInteractions ui) throws Exception {
		CloudApplicationArchiverStrategy strategy = CloudApplicationArchiverStrategies.packageAsJar(project, ui);
		ICloudApplicationArchiver archiver = strategy.getArchiver(new NullProgressMonitor());
		assertNotNull(archiver);
		File jar = archiver.getApplicationArchive(new NullProgressMonitor());
		assertNotNull(jar);
		assertTrue(jar.isFile());
		return jar;
	}

	private BootProjectTestHarness getHarness() {
		return new BootProjectTestHarness(ResourcesPlugin.getWorkspace());
	}

	@Override
	protected String getBundleName() {
		return BootDashTestBundleConstants.BUNDLE_ID;
	}

}
