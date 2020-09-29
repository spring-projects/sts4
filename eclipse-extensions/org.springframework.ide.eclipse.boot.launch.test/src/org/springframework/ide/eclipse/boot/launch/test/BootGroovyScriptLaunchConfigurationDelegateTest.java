/*******************************************************************************
 *  Copyright (c) 2013, 2017 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch.test;

import static org.springframework.ide.eclipse.boot.launch.cli.BootGroovyScriptLaunchConfigurationDelegate.setScript;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.springframework.ide.eclipse.boot.core.cli.BootInstallManager;
import org.springframework.ide.eclipse.boot.launch.cli.BootGroovyScriptLaunchConfigurationDelegate;
import org.springframework.ide.eclipse.boot.test.util.LaunchResult;
import org.springframework.ide.eclipse.boot.test.util.LaunchUtil;

/**
 * @author Kris De Volder
 */
public class BootGroovyScriptLaunchConfigurationDelegateTest extends BootLaunchTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BootInstallManager.getInstance().getDownloader().allowUIThread(true);
	}

	private static final String PROJECT_NAME = BootGroovyScriptLaunchConfigurationDelegateTest.class.getSimpleName();

	private ILaunchConfigurationWorkingCopy createWorkingCopy() throws CoreException {
		return createWorkingCopy(BootGroovyScriptLaunchConfigurationDelegate.ID);
	}

	public void testLaunchHelloWorld() throws Exception {
		IProject p = createGeneralProject(PROJECT_NAME);
		createFile(p, "app.groovy",
				"@Component\n" +
				"class App implements CommandLineRunner {\n" +
				"   void run(String... args) {\n" +
				"       sleep(1000)\n" +
				"       println \"Hello, world!\"\n" +
				"   }\n" +
				"}"
		);

		ILaunchConfigurationWorkingCopy wc = createWorkingCopy();
		setScript(wc, p.getFile("app.groovy"));


		LaunchResult result = LaunchUtil.synchLaunch(wc);
		System.out.println(result); //Great help in debugging this :-)
		assertContains("Hello, world", result.out);
		assertOk(result);
	}
}
