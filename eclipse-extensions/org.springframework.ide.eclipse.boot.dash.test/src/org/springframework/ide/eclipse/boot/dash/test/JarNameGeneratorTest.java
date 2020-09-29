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

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.dash.cf.packaging.JarNameGenerator;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

public class JarNameGeneratorTest extends StsTestCase {

	private JarNameGenerator ng = new JarNameGenerator();

	public void testNamesForJars() {

		assertName("foo-1.2.3.jar", "/home/someplace/foo-1.2.3.jar");
		assertName("foo-1.2.3-1.jar", "/home/some-other-place/foo-1.2.3.jar");
		assertName("foo-1.2.3-2.jar", "/home/and-yet-another/foo-1.2.3.jar");

		//counters are per-name not per JarNameGenerator instance?
		assertName("bar.jar", "/home/bar.jar");
		assertName("bar-1.jar", "/away/bar.jar");

		//Tolerate '.JAR' in upper case?

		assertName("case-nutter.jar", "/somewhere/case-nutter.JAR");
		assertName("case-nutter-1.jar", "/elsewhere/case-nutter.JAR");
	}

	public void testNamesForProjectFolders() throws Exception {
		IProject p = createPredefinedProject("demo-lib");
		IContainer _outputFolder = JavaProjectUtil.getDefaultOutputFolder(JavaCore.create(p));
		File outputFolder = _outputFolder.getLocation().toFile();

		assertName("demo-lib.jar", outputFolder);
		assertName("demo-lib-1.jar", outputFolder);
		assertName("demo-lib-2.jar", outputFolder);
	}

	public void testNamesForNestedProjectFolders() throws Exception {
		IProject p = createPredefinedProject("demo-lib");

		String nestedName = "nested";
		IProject nested = createGeneralProject(p, nestedName, p.getLocation().append(nestedName));

		File dep = nested.getLocation().toFile();

		//The next bit is to check that this is actually a good test. I.e.
		// We do in fact have a situation where a nested project implies that several resources in the
		// workspace represent the same folder on disk.
		IContainer[] containers = getWSRoot().findContainersForLocationURI(dep.toURI());
		assertEquals(2, containers.length);

		//Now check that it makes the right choice and names the jar after 'nested' not after 'demo-lib'.
		assertName("nested.jar", dep);
		assertName("nested-1.jar", dep);
		assertName("nested-2.jar", dep);
	}

	protected IProject createGeneralProject(IProject p, String name, IPath loc) throws CoreException {
		IProjectDescription desc = getWorkspace().newProjectDescription(name);
		IProject nested = getProject(name);
		desc.setLocation(loc);
		nested.create(desc, new NullProgressMonitor());
		return nested;
	}

	private IProject getProject(String name) {
		return getWSRoot().getProject(name);
	}

	protected IWorkspaceRoot getWSRoot() {
		return getWorkspace().getRoot();
	}

	protected IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private void assertName(String expectedName, File dep) {
		assertEquals(expectedName, ng.createName(dep));
	}

	private void assertName(String expectedName, String depPath) {
		assertName(expectedName, new File(depPath));
	}

	@Override
	protected String getBundleName() {
		return BootDashTestBundleConstants.BUNDLE_ID;
	}

}
