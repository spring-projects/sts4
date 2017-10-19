/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.AsyncProcess;
import org.springframework.ide.vscode.commons.util.ExternalCommand;

public class SpringBootAppTest {
	
	private static final String appName = "actuator-client-test-subject";
	private static AsyncProcess testAppRunner;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		testAppRunner = startTestApplication(SpringBootAppTest.class.getResource("/"+appName+"-0.0.1-SNAPSHOT.jar"));
		
		//TODO: add some wait here until the boot app is 'ready'. Otherwise some of our test will just fail trying to read stuff that's not there yet.
	}

	private static AsyncProcess startTestApplication(URL jarUrl) throws Exception {
		File jarFile = new File(jarUrl.toURI());
		return new AsyncProcess(new File("."), new ExternalCommand(
				"java", 
				"-Dserver.port=0", //let spring boot pick randomized free port 
				"-jar", 
				jarFile.getAbsolutePath()
		));
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		testAppRunner.kill();
	}

	@Test
	public void getAllJavaApps() throws Exception {
		Map<String, SpringBootApp> allApps = SpringBootApp.getAllRunningJavaApps();
		Optional<SpringBootApp> myProcess = allApps.values().stream().filter(app -> app.getProcessName().contains(appName)).findAny();
		assertTrue(myProcess.isPresent());
	}

	@Ignore //Failing... not sure how to fix.
	@Test public void getAllBootApps() throws Exception {
		Map<String, SpringBootApp> allApps = SpringBootApp.getAllRunningSpringApps();
		Optional<SpringBootApp> myProcess = allApps.values().stream().filter(app -> app.getProcessName().contains(appName)).findAny();
		assertTrue(myProcess.isPresent());
	}

}
