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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.AsyncProcess;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.test.ACondition;

public class SpringBootAppTest {
	
//	private static final String appName = "actuator-client-15-test-subject"; // Boot 1.5 test app
	private static final String appName = "actuator-client-20-test-subject"; //Boot 2.0 test app
//	private static final String appName = "actuator-client-20-thin-test-subject"; //Boot 2.0 test app with THIN launcher
	
	private static final Duration TIMEOUT = Duration.ofSeconds(30); // in CI build starting the app takes longer than 10s sometimes.
	//Output from CI build: Started ActuatorClientTestSubjectApplication in 22.962 seconds (JVM running for 26.028)

	private static AsyncProcess testAppRunner;
	private static SpringBootApp testApp;
	
	@BeforeClass
	public static void setupClass() throws Exception {
		testAppRunner = startTestApplication(SpringBootAppTest.class.getResource("/"+appName+"-0.0.1-SNAPSHOT.jar"));
		testApp = getAppContaining(appName);
		assertNotNull(testApp);
	}

	private static AsyncProcess startTestApplication(URL jarUrl) throws Exception {
		File jarFile = new File(jarUrl.toURI());
		return new AsyncProcess(
				new File("."), 
				new ExternalCommand(
					"java", 
					"-Dserver.port=0", //let spring boot pick randomized free port 
					"-jar", 
					jarFile.getAbsolutePath()
				),
				false
		);
	}

	private static SpringBootApp getAppContaining(String nameFragment) throws Exception {
		return SpringBootApp.getAllRunningJavaApps().values().stream().filter(app -> app.getProcessName().contains(nameFragment)).findAny().get();
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
	
	@Test
	public void dumpJvmInfo() throws Exception {
		ACondition.waitFor(TIMEOUT, this::getRequestMappings);
		testApp.dumpJvmInfo();
//		SpringBootApp app = getAppContaining("language-server.jar");
//		app.dumpJvmInfo();
	}

	@Ignore //Failing... not sure how to fix.
	@Test public void getAllBootApps() throws Exception {
		Map<String, SpringBootApp> allApps = SpringBootApp.getAllRunningSpringApps();
		Optional<SpringBootApp> myProcess = allApps.values().stream().filter(app -> app.getProcessName().contains(appName)).findAny();
		assertTrue(myProcess.isPresent());
	}
	
	@Test
	public void getPort() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			int port = Integer.parseInt(testApp.getPort());
			assertTrue(port > 0); 
			System.out.println("port = "+port);
		});
	}

	@Test
	public void getHost() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			String host = testApp.getHost(); 
			assertTrue(StringUtil.hasText(host));
			System.out.println("host = "+host);
		});
	}

	@Test
	public void getEnvironment() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			String env = testApp.getEnvironment(); 
			assertNonEmptyJsonObject(env);
			System.out.println("env = "+env);
		});
	}

	@Test
	public void getBeans() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			String beans = testApp.getBeans(); 
			assertNonEmptyJsonObject(beans);
			System.out.println("beans = "+beans);
		});
	}

	@Test
	public void getRequestMappings() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			String result = testApp.getRequestMappings(); 
			assertNonEmptyJsonObject(result);
			System.out.println("requestMappings = "+result);
		});
	}

	@Test
	public void getAutoConfigReport() throws Exception {
		ACondition.waitFor(TIMEOUT, () -> {
			String result = testApp.getAutoConfigReport(); 
			assertNonEmptyJsonObject(result);
			System.out.println("autoconfreport = "+result);
		});
	}

	private void assertNonEmptyJsonObject(String jsonData) {
		JSONObject parsed = new JSONObject(jsonData);
		assertFalse(parsed.keySet().isEmpty());
	}

}
