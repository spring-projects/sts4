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
package org.springframework.ide.eclipse.boot.launch.test;

import static org.springframework.ide.eclipse.boot.test.BootProjectTestHarness.withLanguage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.LaunchResult;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

/**
 * @author Kris De Volder
 */
public class BootLaunchTestCase extends StsTestCase {

	protected BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());

	/**
	 * Create an empty project no nature, no nothing
	 */
	public static IProject createGeneralProject(String name) throws Exception {
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		p.create(new NullProgressMonitor());
		p.open(new NullProgressMonitor());
		assertTrue(p.exists());
		assertTrue(p.isAccessible());
		return p;
	}

	public static void assertOk(LaunchResult result) {
		assertEquals(0, result.terminationCode);
	}

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.boot.launch.test";
	}

	protected ILaunchConfigurationWorkingCopy createWorkingCopy(String launchConfTypeId) throws CoreException {
		String name = DebugPlugin.getDefault().getLaunchManager().generateLaunchConfigurationName("test");
		ILaunchConfigurationWorkingCopy wc = DebugPlugin.getDefault().getLaunchManager()
			.getLaunchConfigurationType(launchConfTypeId)
			.newInstance(null, name);
		return wc;
	}

	public IProject getProject(String name) {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
	}

	public void assertError(String snippet, LiveExpression<ValidationResult> validator) {
		ValidationResult value = validator.getValue();
		assertEquals(IStatus.ERROR, value.status);
		assertContains(snippet, value.msg);
	}

	public static void assertOk(LiveExpression<ValidationResult> validator) {
		ValidationResult status = validator.getValue();
		if (!status.isOk()) {
			fail(status.toString());
		}
	}

	/**
	 * Tests that want to launch something from a project should use this rather to
	 * make sure project is built and has no errors.
	 * <p>
	 * Projects with errors shouldn't be launched as they will just cause launcher tests to
	 * fail in confusing and unpredictabled ways.
	 */
	public IProject createLaunchReadyProject(String projectName) throws Exception {
		IProject project = createPredefinedProject(projectName);
		StsTestUtil.assertNoErrors(project);
		return project;
	}
	
	public IJavaProject createLaunchReadyKotlinProject(final String projectName) throws Exception {
		return JavaCore.create(projects.createBootWebProject(projectName, withLanguage("kotlin")));
	}

}
