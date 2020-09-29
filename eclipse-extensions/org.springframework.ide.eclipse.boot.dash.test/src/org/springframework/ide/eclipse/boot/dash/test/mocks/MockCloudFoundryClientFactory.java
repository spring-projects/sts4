/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.RandomStringUtils;
import org.cloudfoundry.client.CloudFoundryClient;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cf.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshClientSupport;
import org.springframework.ide.eclipse.boot.dash.cf.client.SshHost;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCredentials.CFCredentialType;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFCloudDomainData;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cf.routes.ParsedUri;
import org.springframework.ide.eclipse.boot.dash.cf.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.console.IApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.test.CfTestTargetParams;
import org.springframework.ide.eclipse.boot.dash.test.util.LiveExpToFlux;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import junit.framework.Assert;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MockCloudFoundryClientFactory extends CloudFoundryClientFactory {

	private static void debug(String string) {
		System.out.println(string);
	}

	private final Set<MockClient> instances = Collections.synchronizedSet(new HashSet<>());

	public static final String FAKE_REFRESH_TOKEN = "fakeRefreshToken";
	public static final String FAKE_PASSWORD = CfTestTargetParams.fromEnv("CF_TEST_PASSWORD");
	private Version supportedApiVersion = new Version(CloudFoundryClient.SUPPORTED_API_VERSION);
	private Version apiVersion = supportedApiVersion;

	private Map<String, CFOrganization> orgsByName = new LinkedHashMap<>();
	private Map<String, MockCFSpace> spacesByName = new LinkedHashMap<>();
	private Map<String, CFCloudDomainData> domainsByName = new LinkedHashMap<>();
	private Map<String, MockCFBuildpack> buildpacksByName = new LinkedHashMap<>();
	private Map<String, MockCFStack> stacksByName = new LinkedHashMap<>();

	private Set<String> ssoTokens = new HashSet<>();

	/**
	 * Becomes non-null if notImplementedStub is called, used to check that the tests
	 * only use parts of the mocking harness that are actually implemented.
	 */
	private Exception notImplementedStubCalled = null;
	private long startDelay = 0;

	public MockCloudFoundryClientFactory() {
		defDomain("cfmockapps.io"); //Lost of functionality may assume there's at least one domain so make sure we have one.
		defBuildpacks("java-buildpack", "ruby-buildpack", "funky-buildpack", "another-buildpack");
		defStacks("cflinuxfs2", "windows2012R2");
	}

	synchronized public String getSsoToken() {
		String token = RandomStringUtils.randomAlphabetic(8);
		ssoTokens.add(token);
		return token;
	}

	/**
	 * Verfies the validity of a sso token. Sso token can only be used once
	 * so this check implicitly invalidates the token.
	 *
	 * @return Whether the token was valid prior to the call to this method.
	 */
	private synchronized boolean checkSsoToken(String token) {
		return ssoTokens.remove(token);
	}

	public void defStacks(String... names) {
		for (String n : names) {
			defStack(n);
		}
	}

	public MockCFStack defStack(String name) {
		MockCFStack stack = new MockCFStack(name);
		stacksByName.put(name, stack);
		return stack;
	}

	@Override
	public ClientRequests getClient(CFClientParams params) {
		return new MockClient(params);
	}

	public CFCloudDomain defDomain(String name) {
		CFCloudDomainData it = new CFCloudDomainData(name);
		domainsByName.put(name, it);
		return it;
	}

	public CFCloudDomain defDomain(String name, CFDomainType type, CFDomainStatus status) {
		CFCloudDomainData it = new CFCloudDomainData(name, type, status);
		domainsByName.put(name, it);
		return it;
	}

	public String getDefaultDomain() {
		return domainsByName.keySet().iterator().next();
	}

	public MockCFSpace defSpace(String orgName, String spaceName) {
		String key = orgName+"/"+spaceName;
		MockCFSpace existing = spacesByName.get(key);
		if (existing==null) {
			CFOrganization org = defOrg(orgName);
			spacesByName.put(key, existing= new MockCFSpace(this,
					spaceName,
					UUID.randomUUID(),
					org
			));
		}
		return existing;
	}

	public CFOrganization defOrg(String orgName) {
		CFOrganization existing = orgsByName.get(orgName);
		if (existing==null) {
			orgsByName.put(orgName, existing = new CFOrganizationData(
					orgName,
					UUID.randomUUID()
			));
		}
		return existing;
	}

	public void assertOnlyImplementedStubsCalled() throws Exception {
		if (notImplementedStubCalled!=null) {
			throw notImplementedStubCalled;
		}
	}

	private String validPassword = FAKE_PASSWORD;
	/**
	 * Change the current password this mock client will accept when trying to
	 * use password-based authentication.
	 */
	public void setPassword(String newPassword) {
		this.validPassword = newPassword;
	}

	private class MockClient implements ClientRequests {

		private class MockSshClientSupport implements SshClientSupport {

			@Override
			public SshHost getSshHost() throws Exception {
				return new SshHost("ssh.host.somewhere", 2222, "some-ssh-fingerprint");
			}

			@Override
			public String getSshUser(String appName, int instance) throws Exception {
				MockCFApplication app = getSpace().getApplication(appName);
				if (app==null) {
					throw new IOException("App not found");
				}
				UUID guid = app.getGuid();
				Assert.assertNotNull(guid);
				return getSshUser(guid, instance);
			}

			@Override
			public String getSshCode() throws Exception {
				return "an-ssh-code";
			}

			@Override
			public String getSshUser(UUID appGuid, int instance) throws Exception {
				return appGuid+"/"+instance;
			}

		}

		private int nextPort = 63000;
		private synchronized int choosePort() {
			return nextPort++;
		}

		private CFClientParams params;
		private boolean connected = true;
		private Boolean validCredentials = null;


		private final LiveVariable<String> refreshToken = new LiveVariable<>();

		public MockClient(CFClientParams params) {
			this.params = params;
			instances.add(this);
			debug("created Mock CF Client: "+instances.size());
			refreshToken.addListener((e,v) -> debug("refreshToken <- "+v));
			refreshToken.onDispose(d -> debug("refreshToken DISPOSED"));
		}

		private void notImplementedStub() {
			IllegalStateException e = new IllegalStateException("CF Client Stub Not Yet Implemented");
			if (notImplementedStubCalled==null) {
				notImplementedStubCalled = e;
			}
			throw e;
		}

		@Override
		public Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception {
			checkConnection();
			MockCFSpace space = getSpace();
			return Flux.fromIterable(appsToLookUp)
			.flatMap((app) -> {
				return Mono.justOrEmpty(space.getApplication(app.getGuid()).getDetailedInfo());
			});
		}

		@Override
		public Disposable streamLogs(String appName, IApplicationLogConsole logConsole) throws Exception {
			checkConnection();
			//TODO: This 'log streamer' is a total dummy for now. It doesn't stream any data and canceling it does nothing.
           return Flux.empty().subscribe();
		}

		@Override
		public void stopApplication(String appName) throws Exception {
			checkConnection();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.stop();
		}

		@Override
		public void restartApplication(String appName, CancelationToken cancelationToken) throws Exception {
			checkConnection();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			app.restart(cancelationToken);
		}

		@Override
		public void dispose() {
			connected = false;
			refreshToken.dispose();
			instances.remove(this);
			debug("Mock CF Client disposed: "+instances.size());
		}

		@Override
		public SshClientSupport getSshClientSupport() throws Exception {
			return new MockSshClientSupport();
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<CFSpace> getSpaces() throws Exception {
			checkConnection();
			@SuppressWarnings("rawtypes")
			List hack = ImmutableList.copyOf(spacesByName.values());
			return hack;
		}

		@Override
		public List<CFServiceInstance> getServices() throws Exception {
			checkConnection();
			return getSpace().getServices();
		}

		private MockCFSpace getSpace() throws IOException {
			if (params.getOrgName()==null) {
				throw errorNoOrgSelected();
			}
			if (params.getSpaceName()==null) {
				throw errorNoSpaceSelected();
			}
			MockCFSpace space = spacesByName.get(params.getOrgName()+"/"+params.getSpaceName());
			if (space==null) {
				throw errorSpaceNotFound(params.getOrgName()+"/"+params.getSpaceName());
			}
			return space;
		}

		@Override
		public List<CFCloudDomain> getDomains() throws Exception {
			checkConnection();
			return ImmutableList.<CFCloudDomain>copyOf(domainsByName.values());
		}

		@Override
		public List<CFBuildpack> getBuildpacks() throws Exception {
			checkConnection();
			return ImmutableList.<CFBuildpack>copyOf(buildpacksByName.values());
		}

		@Override
		public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
			checkConnection();
			return getSpace().getApplicationsWithBasicInfo();
		}

		@Override
		public CFApplicationDetail getApplication(String appName) throws Exception {
			checkConnection();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app!=null) {
				return app.getDetailedInfo();
			}
			return null;
		}

		@Override
		public Version getApiVersion() {
			return apiVersion;
		}

		@Override
		public Version getSupportedApiVersion() {
			return supportedApiVersion;
		}

		@Override
		public void deleteApplication(String name) throws Exception {
			checkConnection();
			if (!getSpace().removeApp(name)) {
				throw errorAppNotFound(name);
			}
		}

		@Override
		public String getHealthCheck(UUID appGuid) throws Exception {
			checkConnection();
			MockCFApplication app = getApplication(appGuid);
			if (app == null) {
				throw errorAppNotFound("GUID: "+appGuid.toString());
			} else {
				return app.getHealthCheckType();
			}
		}

		private MockCFApplication getApplication(UUID appGuid) throws IOException {
			return getSpace().getApplication(appGuid);
		}

		/**
		 * Each mock operation that does something requires access to CF should call this
		 * to ensure that it implicitly check whether the connection is valid.
		 * <p>
		 * Operations on 'invalid' connection are expected to throw Exceptions.
		 * Calling this method makes the operations behave as expected. For example,
		 * fail when logged out, or when connection was created with invalid credentials.
		 */
		private void checkConnection() throws Exception {
			if (!connected) {
				throw errorClientNotConnected();
			}
			if (validCredentials==null) {
				validCredentials = isValidCredentials(params.getUsername(), params.getCredentials());
			}
			if (!validCredentials) {
				throw errorInvalidCredentials();
			}
		}

		private boolean isValidCredentials(String username, CFCredentials credentials) throws Exception {
			CFCredentialType type = credentials.getType();
			String secret = credentials.getSecret();
			if (type==CFCredentialType.PASSWORD) {
				if (!credentials.getSecret().equals(validPassword)) {
					return false;
				}
			} else if (type==CFCredentialType.REFRESH_TOKEN) {
				if (!secret.equals(FAKE_REFRESH_TOKEN)) {
					return false;
				}
			} else if (type==CFCredentialType.TEMPORARY_CODE) {
				if (!checkSsoToken(secret)) {
					return false;
				}
			} else {
				return false;
			}
			//Validation of credentials is expected to update refresh token.
			refreshToken.setValue(FAKE_REFRESH_TOKEN);
			return true;
		}

		@Override
		public void setHealthCheck(UUID guid, String hcType) throws Exception {
			checkConnection();
			notImplementedStub();
		}

		@Override
		public List<CFStack> getStacks() throws Exception {
			checkConnection();
			return ImmutableList.<CFStack>copyOf(stacksByName.values());
		}

		@Override
		public boolean applicationExists(String appName) throws Exception {
			checkConnection();
			return getSpace().getApplication(appName) !=null;
		}

		@Override
		public void push(CFPushArguments args, CancelationToken cancelationToken) throws Exception {
			checkConnection();
			System.out.println("Pushing: "+args);
			//TODO: should check services exist and raise an error because non-existant services cannot be bound.
			MockCFSpace space = getSpace();
			MockCFApplication app = new MockCFApplication(MockCloudFoundryClientFactory.this, space, args.getAppName());
			app.setBuildpackUrlMaybe(args.getBuildpack());
			app.setRoutes(buildRoutes(args));

			app.setCommandMaybe(args.getCommand());
			app.setDiskQuotaMaybe(args.getDiskQuota());
			app.setEnvMaybe(args.getEnv());
			app.setMemoryMaybe(args.getMemory());
			app.setServicesMaybe(args.getServices());
			app.setStackMaybe(args.getStack());
			app.setTimeoutMaybe(args.getTimeout());
			app.setHealthCheckTypeMaybe(args.getHealthCheckType());
			app.setHealthCheckHttpEndpoint(args.getHealthCheckHttpEndpoint());
			app.setBits(() -> {
				try {
					return Files.readAllBytes(args.getApplicationDataAsFile().toPath());
				} catch (IOException e) {
					return new byte[0];
				}
			});
			space.put(app);
			space.getPushCount(app.getName()).increment();

			app.start(cancelationToken);
		}

		private Collection<RouteBinding> buildRoutes(CFPushArguments args) {
			List<String> desiredUris = args.getRoutes();
			if (desiredUris!=null) {
				return desiredUris.stream()
						.map(uri -> buildRoute(uri, args))
						.collect(Collectors.toList());
			}
			return ImmutableList.of();
		}

		private RouteBinding buildRoute(String _uri, CFPushArguments args) {
			ParsedUri uri = new ParsedUri(_uri);
			boolean randomRoute = args.getRandomRoute();
			CFCloudDomainData bestDomain = domainsByName.values().stream()
				.filter(domain -> domainCanBeUsedFor(domain, uri))
				.max((d1, d2) -> Integer.compare(d1.getName().length(), d2.getName().length()))
				.orElse(null);
			if (bestDomain==null) {
				throw new IllegalStateException("No domain matching the given uri '"+_uri+"' could be found");
			}
			RouteBinding route = new RouteBinding();
			route.setDomain(bestDomain.getName());
			route.setHost(bestDomain.splitHost(uri.getHostAndDomain()));
			route.setPath(uri.getPath());
			route.setPort(uri.getPort());
			if (randomRoute) {
				if (bestDomain.getType()==CFDomainType.TCP) {
					if (route.getPort()==null) {
						route.setPort(choosePort());
					}
				} else if (bestDomain.getType()==CFDomainType.HTTP) {
					if (route.getHost()==null) {
						route.setHost(chooseHost());
					}
				}
			}
			return route;
		}

		private String chooseHost() {
			return RandomStringUtils.randomAlphabetic(8).toLowerCase();
		}

		private boolean domainCanBeUsedFor(CFCloudDomainData domainData, ParsedUri uri) {
			String domain = domainData.getName();
			String hostAndDomain = uri.getHostAndDomain();
			String host;
			if (!hostAndDomain.endsWith(domain)) {
				return false;
			}
			if (domain.length()==hostAndDomain.length()) {
				//The uri matches domain precisely
				host = null;
			} else if (hostAndDomain.charAt(hostAndDomain.length()-domain.length()-1)=='.') {
				//THe uri matches as ${host}.${domain}
				host = hostAndDomain.substring(0, hostAndDomain.length()-domain.length()-1);
			} else {
				 //Couldn't match this domain to uri
				return false;
			}
			if (domainData.getType()==CFDomainType.TCP) {
				return host==null; //TCP routes don't allow setting a host, only a port
			} else if (domainData.getType()==CFDomainType.HTTP) {
				return uri.getPort()==null; //HTTP routes don't allow setting a port only a host
			} else {
				throw new IllegalStateException("Unknown domain type: "+domainData.getType());
			}
		}

		@Override
		public Map<String, String> getApplicationEnvironment(String appName) throws Exception {
			checkConnection();
			MockCFApplication app = getSpace().getApplication(appName);
			if (app==null) {
				throw errorAppNotFound(appName);
			}
			return ImmutableMap.copyOf(app.getEnv());
		}

		@Override
		public Mono<Void> deleteServiceAsync(String serviceName) {
			return Mono.defer(() -> {
				try {
					checkConnection();
					getSpace().deleteService(serviceName);
					return Mono.empty();
				} catch (Exception e) {
					return Mono.error(e);
				}
			});
		}

		@Override
		public Mono<String> getUserName() {
			return Mono.defer(() -> {
				try {
					checkConnection();
					return Mono.just(params.getUsername());
				} catch (Exception e) {
					return Mono.error(e);
				}
			});
		}

		@Override
		public String getRefreshToken() {
			return refreshToken.getValue();
		}

		@Override
		public Flux<String> getRefreshTokens() {
			return LiveExpToFlux.toFlux(refreshToken);
		}
	}

	public void defBuildpacks(String... names) {
		for (String n : names) {
			defBuildpack(n);
		}
	}

	public MockCFBuildpack defBuildpack(String n) {
		MockCFBuildpack it = new MockCFBuildpack(n);
		buildpacksByName.put(n, it);
		return it;
	}

	//////////////////////////////////////////////////
	// Exception creation methods

	protected IOException errorAppNotFound(String detailMessage) throws IOException {
		return new IOException("App not found: "+detailMessage);
	}

	protected IOException errorClientNotConnected() {
		return new IOException("CF Client not Connected");
	}

	protected IOException errorNoOrgSelected() {
		return new IOException("No org selected");
	}

	protected IOException errorNoSpaceSelected() {
		return new IOException("No space selected");
	}

	protected  IOException errorSpaceNotFound(String detail) {
		return new IOException("Space not found: "+detail);
	}

	protected IOException errorAppAlreadyExists(String detail) {
		return new IOException("App already exists: "+detail);
	}

	protected Exception errorInvalidCredentials() {
		return new Exception("Cannot connect to CF. Invalid credentials.");
	}

	public void setAppStartDelay(TimeUnit timeUnit, int howMany) {
		startDelay = timeUnit.toMillis(howMany);
	}

	/**
	 * @return The delay that a simulated 'start' of an app should take before returning. Given in milliseconds.
	 */
	public long getStartDelay() {
		return startDelay;
	}

	public void setApiVersion(String string) {
		apiVersion = new Version(string);
	}

	public void setSupportedApiVersion(String string) {
		supportedApiVersion = new Version(string);
	}

	public int instanceCount() {
		return instances.size();
	}

	public void changeRefrestToken(String newToken) {
		for (MockClient client : instances) {
			client.refreshToken.setValue(newToken);
		}
	}

}
