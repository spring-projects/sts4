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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.util.AsyncProcess;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.test.ACondition;

import com.google.common.collect.ImmutableList;

public class SpringBootAppTest {

	private static final String[] appNames = {
			"actuator-client-15-test-subject", // Boot 1.5 test app
			"actuator-client-20-test-subject", //Boot 2.0 test app
			"actuator-client-20-thin-test-subject", // Like the Boot 2.0 app, but packaged with thin launcher instead of fatjar
	};

	private static final Duration TIMEOUT = Duration.ofSeconds(60); // in CI build starting the app takes a while, starting several in parallel takes even longer

	private static final List<String> TEST_PROFILES = ImmutableList.of("testing", "funny", "cameleon");

	private static List<AsyncProcess> testAppRunners;

	@BeforeClass
	public static void setupClass() throws Exception {
		testAppRunners = Arrays.asList(appNames).stream().map(appName -> {
			try {
				return startTestApplication(SpringBootAppTest.class.getResource("/boot-apps/"+appName+"-0.0.1-SNAPSHOT.jar"));
			} catch (Exception e) {
				throw ExceptionUtil.unchecked(e);
			}
		})
		.collect(Collectors.toList());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		for (AsyncProcess process : testAppRunners) {
			process.kill();
		}
		testAppRunners = null;
	}

	private static AsyncProcess startTestApplication(URL jarUrl) throws Exception {
		File jarFile = new File(jarUrl.toURI());
		return new AsyncProcess(
				new File("."),
				new ExternalCommand(
					"java",
					"-Dserver.port=0", //let spring boot pick randomized free port
					"-jar",
					jarFile.getAbsolutePath(),
					"--spring.profiles.active="+StringUtil.collectionToCommaDelimitedString(TEST_PROFILES)
				),
				false
		);
	}

	private SpringBootApp getAppContaining(String nameFragment) {
		try {
			return SpringBootApp.getAllRunningJavaApps().stream().filter(app -> app.getProcessName().contains(nameFragment)).findAny().get();
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	@Ignore @Test public void dumpJvmInfo() throws Exception {
		//Ignored because this test may have timing issues. Still useful to
		// run locally and inspect dump results, but may need some tweaking.
		for (String appName : appNames) {
			SpringBootApp testApp = getAppContaining(appName);
			testApp.dumpJvmInfo();
			System.out.println("======================================");
		}
	}

	@Test public void getAllJavaApps() throws Exception {
		Collection<SpringBootApp> allApps = SpringBootApp.getAllRunningJavaApps();
		for (String appName : appNames) {
			Optional<SpringBootApp> myProcess = allApps.stream().filter(app -> app.getProcessName().contains(appName)).findAny();
			assertTrue(appName, myProcess.isPresent());
		}
	}

	@Test public void getAllBootApps() throws Exception {
		Collection<SpringBootApp> allApps = SpringBootApp.getAllRunningSpringApps();
		for (String appName : appNames) {
			Optional<SpringBootApp> myProcess = allApps.stream().filter(app -> app.getProcessName().contains(appName)).findAny();
			assertTrue(myProcess.isPresent());
		}
	}

	@Test
	public void getPort() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			ACondition.waitFor(TIMEOUT, () -> {
				int port = Integer.parseInt(testApp.getPort());
				assertTrue(port > 0);
//				System.out.println("port = "+port);
			});
		}
	}

	private Collection<SpringBootApp> getTestApps() throws Exception {
		return ACondition.waitForValue(TIMEOUT, () -> Arrays.asList(appNames).stream()
				.map(this::getAppContaining)
				.collect(Collectors.toList())
		);
	}

	@Test
	public void getHost() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			System.err.println("getHost for "+testApp);
			ACondition.waitFor(TIMEOUT, () -> {
				String host = testApp.getHost();
				assertTrue(StringUtil.hasText(host));
				System.out.println("host = "+host);
			});
		}
	}

	@Test
	public void getEnvironment() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			ACondition.waitFor(TIMEOUT, () -> {
				String env = testApp.getEnvironment();
				assertNonEmptyJsonObject(env);
				System.out.println("env = "+new JSONObject(env).toString(3));
			});
		}
	}

	@Test
	public void getBeans() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			try {
				ACondition.waitFor(TIMEOUT, () -> {
					LiveBeansModel beansModel = testApp.getBeans();
					assertFalse(beansModel.isEmpty());
		//			System.out.println("beans = "+beans);
				});
			} catch (Throwable e) {
				//Make it easier to identify the culprit of failing test
				throw new RuntimeException("Failed for: "+testApp.getProcessName(), e);
			}
		}
	}

	@Test
	public void getRequestMappings() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			ACondition.waitFor(TIMEOUT, () -> {
				Collection<RequestMapping> result = testApp.getRequestMappings();
				assertTrue(result != null && !result.isEmpty());
//				System.out.println("requestMappings = "+result);
			});
		}
	}

	@Test
	public void getLiveConditionals() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			try {
				ACondition.waitFor(TIMEOUT, () -> {
					Optional<List<LiveConditional>> result = testApp.getLiveConditionals();
					assertTrue(result.isPresent());
					assertFalse(result.get().isEmpty());
				});
			} catch (Exception e) {
				throw new RuntimeException("Failed for: "+testApp, e);
			}
		}
	}

	@Test
	public void getProfiles() throws Exception {
		for (SpringBootApp testApp : getTestApps()) {
			ACondition.waitFor(TIMEOUT, () -> {
				List<String> result = testApp.getActiveProfiles();
				assertEquals(ImmutableList.copyOf(TEST_PROFILES), result);
			});
		}
	}

	private void assertNonEmptyJsonObject(String jsonData) {
		JSONObject parsed = new JSONObject(jsonData);
		assertFalse(parsed.keySet().isEmpty());
	}

}
