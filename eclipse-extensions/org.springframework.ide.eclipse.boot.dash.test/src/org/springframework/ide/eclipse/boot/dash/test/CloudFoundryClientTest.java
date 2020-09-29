/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.ide.eclipse.boot.dash.test.CloudFoundryTestHarness.APP_DEPLOY_TIMEOUT;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.HealthChecks;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultClientRequestsV2;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.DefaultCloudFoundryClientFactoryV2;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.ReactorUtils;
import org.springframework.ide.eclipse.boot.dash.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.test.util.SslValidationDisabler;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.test.BootProjectTestHarness;
import org.springframework.ide.eclipse.boot.test.util.TestBracketter;
import org.springframework.ide.eclipse.boot.util.RetryUtil;
import org.springframework.ide.eclipse.boot.util.Thunk;
import org.springsource.ide.eclipse.commons.core.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import junit.framework.AssertionFailedError;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CloudFoundryClientTest {

	private static final String WDC_06_PCF2 = "wdc-06-pcf2-system.oc.vmware.com";

	public String CFAPPS_IO() {
		return get_CFAPPS_IO(clientParams);
	}

	public static String get_CFAPPS_IO(CFClientParams clientParams) {
		String org = clientParams.getOrgName();
		String api = clientParams.getApiUrl();
		if (org.equals("application-platform-testing")) {
			//PWS test space/org
			return "cfapps.io";
		} else if (org.equals("pivot-kdevolder")) {
			//PEZ
			return "cfapps.pez.pivotal.io";
		} else if (api.contains("api.tan.")) {
			//TAN
			return "tan.springapps.io";
		} else if (api.contains(WDC_06_PCF2)) {
			//vmware pws spring/sts-test space
			return "wdc-06-pcf2-apps.oc.vmware.com";
		}
		throw new AssertionFailedError("unknown test environment, not sure what to expect here");
	}

	public CFCloudDomain[] getExpectedDomains() {
		String org = clientParams.getOrgName();
		String api = clientParams.getApiUrl();
		if (org.equals("application-platform-testing")) {
			//PWS test space/org
			return new CFCloudDomain[] {
					new CFCloudDomainData("cfapps.io")
			};
		} else if (org.equals("pivot-kdevolder")) {
			//PEZ
			return new CFCloudDomain[] {
					new CFCloudDomainData("cfapps.pez.pivotal.io"),
					new CFCloudDomainData("pezapp.io")
			};
		} else if (api.contains("api.tan.")) {
			//TAN
			return new CFCloudDomain[] {
					new CFCloudDomainData("tan.springapps.io")
			};
		} else if (api.contains(WDC_06_PCF2)) {
			//vmware pws spring/sts-test space
			return new CFCloudDomain[] {
					new CFCloudDomainData("wdc-06-pcf2-apps.oc.vmware.com")
			};
		}
		throw new AssertionFailedError("unknown test environment, not sure what to expect here");
	}

	public String[] getExectedBuildpacks() {
		String org = clientParams.getOrgName();
		String api = clientParams.getApiUrl();
		if (org.equals("application-platform-testing")) {
			//PWS test space/org
			return new String[] {
				"staticfile_buildpack",
				"java_buildpack",
				"ruby_buildpack"
			};
		} else if (org.equals("pivot-kdevolder")) {
			//PEZ
			return new String[] {
				"staticfile_buildpack",
				"java_buildpack_offline",
				"ruby_buildpack"
			};
		} else if (api.contains("api.tan.")) {
			//TAN
			return new String[] {
				"staticfile_buildpack",
				"java_buildpack_offline",
				"ruby_buildpack"
			};
		} else if (api.contains(WDC_06_PCF2)) {
			return new String[] {
					"staticfile_buildpack",
					"java_buildpack_offline",
					"ruby_buildpack"
			};
		}
		throw new AssertionFailedError("unknown test environment, not sure what to expect here");
	}

	public String getExpectedSshHost() {
		String org = clientParams.getOrgName();
		String api = clientParams.getApiUrl();
		if (org.equals("application-platform-testing")) {
			//PWS
			return "ssh.run.pivotal.io";
		} else if (api.contains("api.tan.")) {
			//TAN
			return "ssh.tan.springapps.io";
		} else if (api.contains(WDC_06_PCF2)) {
			return "ssh."+WDC_06_PCF2;
		}
		throw new AssertionFailedError("unknown test environment, not sure what to expect here");
	}

	public static final Predicate<Throwable> FLAKY_SERVICE_BROKER = (e) -> {
		String msg = ExceptionUtil.getMessage(e).toLowerCase();
		return msg.contains("500")
			|| msg.contains("502")
			;
	};

	private CFClientParams clientParams = CfTestTargetParams.fromEnv();
	private DefaultClientRequestsV2 client = createClient(clientParams);

	public TestBracketter bracketer = new TestBracketter();
	public CloudFoundryServicesHarness services = new CloudFoundryServicesHarness(clientParams, client);
	public CloudFoundryApplicationHarness appHarness = new CloudFoundryApplicationHarness(client);

	@Before
	public void setup() throws Exception {
		SslValidationDisabler.disableSslValidation();
		ReactorUtils.DUMP_STACK_ON_TIMEOUT = true;
	}

	@After
	public void teardown() throws Exception {
		appHarness.dispose(); //apps first because services still bound to apps can't be deleted!
		services.dispose();
		if (client!=null) {
			client.close();
		}
		StsTestUtil.cleanUpProjects();
		ReactorUtils.DUMP_STACK_ON_TIMEOUT = false;
	}

	public BootProjectTestHarness projects = new BootProjectTestHarness(ResourcesPlugin.getWorkspace());

	private UserInteractions ui = Mockito.mock(UserInteractions.class);

	public static DefaultClientRequestsV2 createClient(CFClientParams params) {
		try {
			DefaultCloudFoundryClientFactoryV2 factory = DefaultCloudFoundryClientFactoryV2.INSTANCE;
			return (DefaultClientRequestsV2) factory.getClient(params);
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	@Rule
	public TestBracketter testBrack = new TestBracketter();

	@Test
	public void testGetApiVersion() throws Exception {
		Version version = client.getApiVersion();
		System.out.println("Api version = "+version);
		assertNotNull(version);
	}

	@Test
	public void testGetSpaces() throws Exception {
		int success = 0;
		int failed  = 0;

		Exception error = null;
		for (int i = 0; i < 5; i++) {
			try {
				long start = System.currentTimeMillis();
				List<CFSpace> spaces = client.getSpaces();
				long duration = System.currentTimeMillis() - start;
				System.out.println("getSpaces -> "+spaces.size()+" spaces in "+ duration + " ms");
				success++;
			} catch (Exception e) {
				error = e;
				failed++;
				System.out.println("getSpaces -> "+ExceptionUtil.getMessage(e));
			}
		}
		System.out.println("getSpaces failure rate = "+failed + "/" +(success+failed));
		if (failed>0) {
			throw new IOException("getSpaces failure rate = "+failed + "/" +(success+failed), error);
		}
	}

	@Test
	public void testGetApplicationDetails() throws Exception {
		String appName = appHarness.randomAppName();

		try (CFPushArguments params = new CFPushArguments()) {
			params.setAppName(appName);
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			params.setNoStart(true);
			push(params);
		}

		{
			CFApplicationDetail appDetails = client.getApplication(appName);
			assertEquals(0, appDetails.getRunningInstances());
			assertEquals(CFAppState.STOPPED, appDetails.getState());
			assertEquals(ImmutableList.of(), appDetails.getInstanceDetails());
		}

		client.restartApplication(appName, CancelationTokens.NULL);
		{
			CFApplicationDetail appDetails = client.getApplication(appName);
			assertEquals(1, appDetails.getRunningInstances());
			assertEquals(CFAppState.STARTED, appDetails.getState());
			assertEquals(1, appDetails.getInstanceDetails().size());
		}
	}

	@Test
	public void testPushAndBindServices() throws Exception {
		//This test fails occasionally because service binding is 'unreliable'. Had a long discussion
		// with Ben Hale. The gist is errors happen and should be expected in distributed world.
		//They are coming from 'AppDirect' which manages the services. The errors are mediated through cloudfoundry
		// which doesn't knwow how it should handle them. So it passed the buck onto the its callers.
		//In this case.... cf-java-client which does the same thing and passes them to us.
		//All the reasons why they can't handle these errors also apply to us, which means that
		//the operation is simply unreliable and so failure is an expected outcome even when everything
		//works correctly.
		//To avoid this test case from failing too often we retry it a few times.
		RetryUtil.retryTimes("testPushAndBindServices", 4, () -> {

			System.out.println("Executing full test scenario.");
			String service1 = services.createTestService();
			String service2 = services.createTestService();
			String service3 = services.createTestService(); //An extra unused service (makes this a better test).

			String appName = appHarness.randomAppName();
			CFPushArguments params = new CFPushArguments();
			params.setAppName(appName);
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			params.setServices(ImmutableList.of(service1, service2));
			push(params);

			assertEquals(ImmutableSet.of(service1, service2), getBoundServiceNames(appName));

			client.bindAndUnbindServices(appName, ImmutableList.of(service1)).block();
			assertEquals(ImmutableSet.of(service1), getBoundServiceNames(appName));

			client.bindAndUnbindServices(appName, ImmutableList.of(service2)).block();
			assertEquals(ImmutableSet.of(service2), getBoundServiceNames(appName));

			client.bindAndUnbindServices(appName, ImmutableList.of()).block();
			assertEquals(ImmutableSet.of(), getBoundServiceNames(appName));
		});
	}

	private Set<String> getBoundServiceNames(String appName) throws Exception {
		return client.getBoundServicesSet(appName).block();
	}

	@Test
	public void testPushAndBindHostAndDomain() throws Exception {
		String appName = appHarness.randomAppName();

		for (int i = 0; i < 2; i++) {
			//Why this loop? Because there was bug which CF V2 that made second push fail to bind to
			// map a host that was previously mapped.
			if (i>0) {
				System.out.println("Delete app");
				client.deleteApplication(appName);
			}

			System.out.println("Pushing "+(i+1));
			CFPushArguments params = new CFPushArguments();
			params.setAppName(appName);
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			params.setRoutes(ImmutableList.of(appName+"."+CFAPPS_IO()));

			push(params);
		}

		System.out.println("Pushing SUCCESS");

		CFApplicationDetail app = client.getApplication(appName);
		assertNotNull("Expected application to exist after push: " + appName, app);

		assertEquals(ImmutableSet.of(appName+"."+CFAPPS_IO()), ImmutableSet.copyOf(app.getUris()));
	}

	@Test
	public void testPushAndBindMultipleHosts() throws Exception {
		String[] hostNames = {
				appHarness.randomAppName(),
				appHarness.randomAppName()
		};
		String appName = hostNames[0];

		CFPushArguments params = new CFPushArguments();
		params.setAppName(hostNames[0]);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");

		Set<String> routes = ImmutableSet.copyOf(Stream.of(hostNames)
				.map((host) -> host + "." + CFAPPS_IO())
				.collect(Collectors.toList())
		);
		params.setRoutes(routes);

		push(params);

		System.out.println("Pushing SUCCESS");

		CFApplicationDetail app = client.getApplication(appName);
		assertNotNull("Expected application to exist after push: " + appName, app);

		assertEquals(routes, ImmutableSet.copyOf(app.getUris()));
	}

	@Test
	public void testPushAndSetRoutes() throws Exception {
		String[] hostNames = {
				appHarness.randomAppName(),
				appHarness.randomAppName()
		};
		String appName = hostNames[0];

		CFPushArguments params = new CFPushArguments();
		params.setAppName(hostNames[0]);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");

		Set<String> routes = ImmutableSet.copyOf(Stream.of(hostNames)
				.map((host) -> host + "." + CFAPPS_IO())
				.collect(Collectors.toList())
		);
		params.setRoutes(routes);

		push(params);

		System.out.println("Pushing SUCCESS");

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertNotNull("Expected application to exist after push: " + appName, app);
			assertEquals(routes, ImmutableSet.copyOf(app.getUris()));
		}

		doSetRoutesTest(appName, ImmutableSet.of(), params.getRandomRoute());

		for (String route : routes) {
			doSetRoutesTest(appName, ImmutableSet.of(route), params.getRandomRoute());
		}

	}

	private void doSetRoutesTest(String appName, ImmutableSet<String> routes, boolean randomRoute) throws Exception {
		ReactorUtils.get(client.setRoutes(appName, routes, randomRoute));
		CFApplicationDetail app = client.getApplication(appName);
		assertEquals(routes, ImmutableSet.copyOf(app.getUris()));
	}

	@Test
	public void testPushWithBasicHealthcheckTypes() throws Exception {
		//This test is to make sure that hc info is properly passed on by push operation
		// to the 'real' cf client.
		//Since the push works different on firt push and repush we have to check both!

		String appName = appHarness.randomAppName();

		String[] HC_TYPES = {
				HealthChecks.HC_PORT,
				HealthChecks.HC_PROCESS
		};

		for (String hcType : HC_TYPES) {
			try (CFPushArguments params = new CFPushArguments()) {
				params.setAppName(appName);
				params.setRoutes(appName+"."+CFAPPS_IO());
				params.setApplicationData(getTestZip("testapp"));
				params.setBuildpack("staticfile_buildpack");
				params.setHealthCheckType(hcType);
				push(params);
				CFApplicationDetail app = client.getApplication(appName);
				assertNotNull("Expected application to exist after push: " + appName, app);
				assertEquals(hcType, app.getHealthCheckType());
			}
		}
	}

	@Test
	public void testPushAndSetHealthcheckHttpEndpoint() throws Exception {
		String appName = appHarness.randomAppName();

		try (CFPushArguments params = new CFPushArguments()) {
			params.setAppName(appName);
			params.setRoutes(appName+"."+CFAPPS_IO());
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			params.setHealthCheckHttpEndpoint("/test.txt");
			params.setHealthCheckType(HealthChecks.HC_HTTP);
			push(params);
			CFApplicationDetail app = client.getApplication(appName);
			assertNotNull("Expected application to exist after push: " + appName, app);
			assertEquals(HealthChecks.HC_HTTP, app.getHealthCheckType());
			assertEquals("/test.txt", app.getHealthCheckHttpEndpoint());
		}
	}

	@Test
	public void testPushAndSetEnv() throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setRoutes(appName+"."+CFAPPS_IO());
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setEnv(ImmutableMap.of(
				"foo", "foo_value",
				"bar", "bar_value"
		));
		push(params);

		CFApplicationDetail app = client.getApplication(appName);
		assertNotNull("Expected application to exist after push: " + appName, app);
		ACondition.waitFor("app content to be availabe", 10_000, () -> {
			String content = IOUtils.toString(new URI("https://" + appName + '.' + CFAPPS_IO() + "/test.txt"));
			assertTrue(content.length() > 0);
			assertTrue(content.contains("content"));
		});

		{
			Map<String, String> env = client.getEnv(appName).block();
			assertEquals("foo_value", env.get("foo"));
			assertEquals("bar_value", env.get("bar"));
			assertEquals(2, env.size());
		}

		client.setEnvVars(appName, ImmutableMap.of("other", "value")).block();
		{
			Map<String, String> env = client.getEnv(appName).block();
			assertEquals("value", env.get("other"));
			assertEquals(1, env.size());
		}

		//This last piece is commented because it fails.
		//See: https://www.pivotaltracker.com/story/show/116804259

		// The last var doesn't get removed. Not sure how to fix it.
		// But eventually we won't even be using 'setEnvVars' it will be part of the push.
		// and its not going to be our problem to fix that.
//		client.updateApplicationEnvironment(appName, ImmutableMap.of()).get();
//		{
//			Map<String, Object> env = client.getEnv(appName).get();
//			assertEquals(0, env.size());
//		}
	}

	@Test
	public void testDeleteApplication()	 throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		push(params);

		CFApplicationDetail app = client.getApplication(appName);
		assertTrue(client.applicationExists(appName));
		assertNotNull("Expected application to exist after push: " + appName, app);

		client.deleteApplication(appName);
		app = client.getApplication(appName);
		assertNull("Expected application to be deleted after delete: " + appName, app);
		assertFalse(client.applicationExists(appName));
	}

	@Test
	public void testStopApplication()	 throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		push(params);

		final CFApplicationDetail runningApp = client.getApplication(appName);
		assertNotNull("Expected application to exist after push: " + appName, runningApp);

		new ACondition("wait for app '"+ appName +"'to be RUNNING", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				assertAppRunState(1, runningApp.getRunningInstances(), CFAppState.STARTED, runningApp.getState());
				return true;
			}
		};

		client.stopApplication(appName);
		final CFApplicationDetail stoppedApp = client.getApplication(appName);

		new ACondition("wait for app '"+ appName +"'to be STOPPED", APP_DEPLOY_TIMEOUT) {
			public boolean test() throws Exception {
				assertAppRunState(0, stoppedApp.getRunningInstances(), CFAppState.STOPPED, stoppedApp.getState());
				return true;
			}
		};
	}

	@Test
	public void getServices() throws Exception {
		RetryUtil.retryWhen("getServices", 5, FLAKY_SERVICE_BROKER,
			() -> {
				String[] serviceNames = new String[3];
				Set<String> userProvided = new HashSet<>();
				for (int i = 0; i < serviceNames.length; i++) {
					if (i%2==0) {
						serviceNames[i] = services.createTestService();
					} else {
						serviceNames[i] = services.createTestUserProvidedService();
						userProvided.add(serviceNames[i]);
					}
				}

				List<CFServiceInstance> actualServices = client.getServices();
				ImmutableSet<String> actualServiceNames = ImmutableSet.copyOf(
						actualServices.stream()
						.map(CFServiceInstance::getName)
						.collect(Collectors.toList())
				);


				for (CFServiceInstance s : actualServices) {
					//Note: because these test on CI host run in parallel with others using same space...
					// we should assume there might be 'extra stuff' on the space than what we just created here.
					//So only check that 'our stuff' exists and looks right and ignore the rest.
					String name = s.getName();
					if (ImmutableSet.copyOf(serviceNames).contains(name)) {
						System.out.println("Verifying service: "+name);
						if (userProvided.contains(name)) {
							assertEquals("user-provided", s.getService());
							System.out.println("  user provided => OK");
						} else {
							assertEquals(services.getTestServiceAndPlan()[0], s.getService());
							System.out.println("  getService() => OK");
							String expectPlan = services.getTestServiceAndPlan()[1];
							assertEquals(expectPlan, s.getPlan());
							System.out.println("  getPlan() => OK");
							assertIsURL(s.getDashboardUrl());
							System.out.println("  getDashboardUrl() => OK");
							assertIsURL(s.getDocumentationUrl());
							System.out.println("  getDocumentationUrl() => OK");
							assertText(s.getDescription());
							System.out.println("  getDescription() => OK");
						}
					}
				}

				for (String s : serviceNames) {
					assertTrue(s+" not found in "+actualServiceNames, actualServiceNames.contains(s));
				}
			}
		);
	}

	@Test
	public void testServiceCreateAndDelete() throws Exception {
		RetryUtil.retryWhen("testServiceCreateAndDelete", 5, FLAKY_SERVICE_BROKER, () -> {
			String[] serviceNames = new String[2];
			for (int i = 0; i < serviceNames.length; i++) {
				serviceNames[i] = services.randomServiceName();
			};
			for (int i = 0; i < serviceNames.length; i++) {
				String serviceName = serviceNames[i];
				if (i%2==0) {
					System.out.println("Create service: "+serviceName);
					String[] serviceInfo = services.getTestServiceAndPlan();
					client.createService(serviceName, serviceInfo[0], serviceInfo[1])
					.block(CloudFoundryServicesHarness.CREATE_SERVICE_TIMEOUT);
				} else {
					System.out.println("Create user-provided service: "+serviceName);
					client.createUserProvidedService(serviceName, ImmutableMap.of())
					.block();
				}
			}

			List<CFServiceInstance> services = client.getServices();
			assertServices(services, serviceNames);
			for (String serviceName : serviceNames) {
				client.deleteServiceAsync(serviceName).block();
				System.out.println("Deleted service: "+serviceName);
			}

			assertNoServices(client.getServices(), serviceNames);
		});
	}

	@Test
	public void testGetBoundServices() throws Exception {
		RetryUtil.retryWhen("testGetBoundServices", 5, FLAKY_SERVICE_BROKER, () -> {
			String service1 = services.createTestService();
			String service2 = services.createTestService();
			String service3 = services.createTestService();

			String appName = appHarness.randomAppName();
			CFPushArguments params = new CFPushArguments();
			params.setAppName(appName);
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			params.setServices(ImmutableList.of(service1, service2));
			push(params);

			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals(ImmutableSet.of(service1, service2), ImmutableSet.copyOf(app.getServices()));

			app = client.getApplication(appName);
			assertEquals(ImmutableSet.of(service1, service2), ImmutableSet.copyOf(app.getServices()));
		});
	}


	@Test
	public void testGetDomains() throws Exception {
		CFClientParams params = CfTestTargetParams.fromEnv();
		client = createClient(params);
		List<CFCloudDomain> domains = client.getDomains();

		for (CFCloudDomain d : domains) {
			System.out.println(d.getName()+"\t"+d.getType());
		}

		assertContains(domains, getExpectedDomains());

		assertEquals(CFAPPS_IO(), domains.stream()
				.filter(d -> d.getStatus()==CFDomainStatus.SHARED && d.getType()==CFDomainType.HTTP)
				.map(d -> d.getName())
				.findFirst()
				.orElse(null)
		);
	}

	@Test
	public void testGetBuildpacks() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());
		List<CFBuildpack> buildpacks = client.getBuildpacks();

		Set<String> names = Flux.fromIterable(buildpacks)
				.map(CFBuildpack::getName)
				.collectList()
				.map(ImmutableSet::copyOf)
				.block();

		assertContains(names, getExectedBuildpacks());
	}

	@Test
	public void testGetStacks() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());
		List<CFStack> stacks = client.getStacks();

		Set<String> names = Flux.fromIterable(stacks)
			.map(CFStack::getName)
			.collectList()
			.map(ImmutableSet::copyOf)
			.block();

		assertContains(names,
				"cflinuxfs3"
		);
	}

	@Test
	public void testApplicationLogConnection() throws Exception {
		client = createClient(CfTestTargetParams.fromEnv());

		String appName = appHarness.randomAppName();
		IApplicationLogConsole listener = mock(IApplicationLogConsole.class);
		Disposable token = client.streamLogs(appName, listener);
		assertNotNull(token);

		Future<Void> pushResult = doAsync(() -> {
			CFPushArguments params = new CFPushArguments();
			params.setAppName(appName);
			params.setApplicationData(getTestZip("testapp"));
			params.setBuildpack("staticfile_buildpack");
			push(params);
		});

		ACondition.waitFor("push", TimeUnit.MINUTES.toMillis(4), () -> {
			assertTrue(pushResult.isDone());
		});
		pushResult.get();

		BootDashModelTest.waitForJobsToComplete();
		verify(listener, atLeastOnce()).onMessage(any());
	}

//	@Test
//	public void testGetExistingRoutes() throws Exception {
//		String appName = "foo";//appHarness.randomAppName();
//
////		CFPushArguments params = new CFPushArguments();
////		params.setAppName(appName);
////		params.setApplicationData(getTestZip("testapp"));
////		params.setBuildpack("staticfile_buildpack");
////		push(params);
//
//		for (int i = 0; i < 3; i++) {
//			System.out.println("====================");
//			assertTrue(client.getExistingRoutes(appName).toList().get().isEmpty());
//		}
//	}



	@Test
	public void testGetApplicationBuildpack() throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals("staticfile_buildpack", app.getBuildpackUrl());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals("staticfile_buildpack", app.getBuildpackUrl());
		}
	}


	@Test
	public void testGetApplicationStack() throws Exception {
		String appName = appHarness.randomAppName();
		String stackName = "cflinuxfs3";

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setStack(stackName);
		push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals(stackName, app.getStack());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals(stackName, app.getStack());
		}
	}

	@Test
	public void testGetApplicationTimeout() throws Exception {
		String appName = appHarness.randomAppName();
		int timeout = 67;

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setTimeout(timeout);
		push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals(timeout, (int)app.getTimeout());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals(timeout, (int)app.getTimeout());
		}
	}

	@Test
	public void testRandomHost() throws Exception {
		//It is now the responsibility of the client to interpret the 'random route' attribute and
		// generate random host or port. This test checks if it does that.
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setRoutes(CFAPPS_IO());
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setRandomRoute(true);
		push(params);

		CFApplicationDetail app = client.getApplication(appName);
		List<String> uris = app.getUris();
		assertEquals(1, uris.size());
		String uri = uris.get(0);
		String host = uri.split("\\.")[0];
		String domain = uri.substring(host.length()+1);
		assertEquals(CFAPPS_IO(), domain);
		assertTrue(StringUtil.hasText(host));
	}

	@Test
	public void testGetApplicationCommand() throws Exception {
		String appName = appHarness.randomAppName();
		String command = "something interesting";

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		params.setCommand(command);
		params.setNoStart(true); // Our command is bogus so starting won't work
		push(params);

		//Note we try to get the app two different ways because retrieving the info in
		// each case is slightly different.

		{
			CFApplicationDetail app = client.getApplication(appName);
			assertEquals(command, app.getCommand());
		}

		{
			List<CFApplication> allApps = client.getApplicationsWithBasicInfo();
			CFApplication app = null;
			for (CFApplication a : allApps) {
				if (a.getName().equals(appName)) {
					app = a;
				}
			}
			assertEquals(command, app.getCommand());
		}
	}

	@Test public void testSshSupport() throws Exception {
		String appName = appHarness.randomAppName();

		CFPushArguments params = new CFPushArguments();
		params.setAppName(appName);
		params.setApplicationData(getTestZip("testapp"));
		params.setBuildpack("staticfile_buildpack");
		push(params);

		SshClientSupport sshSupport = client.getSshClientSupport();
		SshHost sshHost = sshSupport.getSshHost();
		System.out.println(sshHost);
		assertEquals(getExpectedSshHost(), sshHost.getHost());
		assertEquals(2222, sshHost.getPort());
		assertTrue(StringUtil.hasText(sshHost.getFingerPrint()));

		assertTrue(StringUtil.hasText(sshSupport.getSshCode()));
		UUID appGuid = client.getApplication(appName).getGuid();
		String sshUser = sshSupport.getSshUser(appGuid, 0);
		System.out.println("sshUser = "+sshUser);
		assertTrue(StringUtil.hasText(sshUser));

		String code = sshSupport.getSshCode();
		System.out.println("sshCode = "+code);
		assertTrue(StringUtil.hasText(code));
	}

	@Test public void testGetServiceDashboardUrl() throws Exception {
		String serviceName = services.createTestService();
		CFServiceInstance service = null;
		for (CFServiceInstance s : client.getServices()) {
			if (s.getName().equals(serviceName)) {
				service = s;
			}
		}
		String dashUrl = service.getDashboardUrl();
		assertNotNull(dashUrl);
		assertTrue(dashUrl.startsWith("https"));
	}

	@Test public void startCanBeCanceled() throws Exception {
		IProject project = projects.createBootWebProject("slow-starter");
		File jarFile = BootJarPackagingTest.packageAsJar(project, ui);
		System.out.println();

		String appName = appHarness.randomAppName();
		try (CFPushArguments params = new CFPushArguments()) {
			params.setAppName(appName);
			params.setRoutes(appName+"."+CFAPPS_IO());
			params.setApplicationData(jarFile);
			params.setNoStart(true);

			long starting = System.currentTimeMillis();
			System.out.println("Pushing...");
			push(params);
			long duration = System.currentTimeMillis() - starting;
			System.out.println("Pushing took: "+duration+ " ms");
		}

		CancelationTokens cancelationTokens = new CancelationTokens();
		long starting = System.currentTimeMillis();
		System.out.println("Starting...");
		Future<Void> startResult = doAsync(() -> {
			client.restartApplication(appName, cancelationTokens.create());
			long duration = System.currentTimeMillis() - starting;
			System.out.println("started in "+duration+" ms");
		});

		Thread.sleep(5000);
		long cancelTime = System.currentTimeMillis();
		cancelationTokens.cancelAll();
		try {
			startResult.get(5, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			e.printStackTrace();
			long duration = System.currentTimeMillis() - cancelTime;
			assertEquals(OperationCanceledException.class, ExceptionUtil.getDeepestCause(e).getClass());
			System.out.println("\nRestart Canceled after "+duration+" ms");
		}
	}

	@Test public void pushCanBeCanceled() throws Exception {
		String appName = appHarness.randomAppName();
		IProject project = projects.createBootWebProject("slow-starter");
		File jarFile = BootJarPackagingTest.packageAsJar(project, ui);

		CancelationTokens cancelationTokens = new CancelationTokens();
		try (CFPushArguments params = new CFPushArguments()) {
			params.setAppName(appName);
			params.setRoutes(appName+"."+CFAPPS_IO());
			params.setApplicationData(jarFile);

			long starting = System.currentTimeMillis();
			Future<Void> pushResult = doAsync(() -> {
				System.out.println("Pushing...");
				client.push(params, cancelationTokens.create());
				long duration = System.currentTimeMillis() - starting;
				System.out.println("Pushing took: "+duration+ " ms");
			});
			Thread.sleep(Duration.ofSeconds(10).toMillis());
			long cancelTime = System.currentTimeMillis();
			System.out.println("Canceling...");
			cancelationTokens.cancelAll();

			try {
				pushResult.get(5, TimeUnit.SECONDS); // Cancel should happen pretty 'fast'!
				fail("push completed but it should have been canceled");
			} catch (ExecutionException e) { // real exception is wrapped in EE by Future.get
				e.printStackTrace();
				long duration = System.currentTimeMillis() - cancelTime;
				assertEquals(OperationCanceledException.class, ExceptionUtil.getDeepestCause(e).getClass());
				System.out.println("\nPush Canceled after: "+duration +" ms");
			}
		}
	}


	/////////////////////////////////////////////////////////////////////////////

	private void assertText(String s) {
		if (!StringUtil.hasText(s)) {
			fail("Found no text, but expected some");
		}
	}

	private void assertIsURL(String url) throws URISyntaxException {
		assertText(url);
		new URI(url); //parse it
	}

	private Future<Void> doAsync(Thunk task) {
		CompletableFuture<Void> result = new CompletableFuture<>();
		Job job = new Job("Async task") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					task.call();
					result.complete(null);
				} catch (Throwable e) {
					result.completeExceptionally(e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return result;
	}

	private void push(CFPushArguments _params) throws Exception {
		if (_params.getMemory() == null) {
			_params.setMemory(1024);
		}
		try (CFPushArguments params = _params) {
			client.push(params, CancelationTokens.NULL);
		}
	}

	private <T> void assertContains(Collection<T> strings, @SuppressWarnings("unchecked") T... expecteds) {
		for (T e : expecteds) {
			assertContains(e, strings);
		}
	}

	private <T> void assertContains(T expected, Collection<T> set) {
		assertTrue("Expected '"+expected+"' not found in: "+set, set.contains(expected));
	}

	private void assertNoServices(List<CFServiceInstance> services, String... serviceNames) throws Exception {
		Set<String> names = services.stream().map(CFServiceInstance::getName).collect(Collectors.toSet());
		for (String serviceName : serviceNames) {
			assertFalse("Service exists but shouldn't: "+serviceName, names.contains(serviceName));
		}
	}

	private void assertServices(List<CFServiceInstance> services, String... serviceNames) throws Exception {
		Set<String> names = services.stream().map(CFServiceInstance::getName).collect(Collectors.toSet());
		assertContains(names, serviceNames);
	}

	private void assertAppRunState(int expectedInstances, int actualInstances, CFAppState expectedRequestedState, CFAppState actualRequestedState) {
		assertEquals("Expected running instances does not match actual running instances: ", expectedInstances, actualInstances);
		assertEquals("Expected requested app state does not match actual requested app state: ", expectedRequestedState, actualRequestedState);
	}

	private File getTestZip(String fileName) {
		File sourceWorkspace = new File(
				StsTestUtil.getSourceWorkspacePath("org.springframework.ide.eclipse.boot.dash.test"));
		File file = new File(sourceWorkspace, fileName + ".zip");
		Assert.isTrue(file.exists(), ""+ file);
		return file;
	}

}
