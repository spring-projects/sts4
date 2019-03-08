/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.cloudfoundry.uaa.UaaException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo.TargetDiagnosticMessages;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ConnectionException;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.NoTargetsException;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

public class CFClientTest {

	MockCfCli cloudfoundry = new MockCfCli();
	ClientTimeouts timeouts = new ClientTimeouts();
	CFTargetCache targetCache;
	TargetDiagnosticMessages expectedMessages = CfCliParamsProvider.CLI_PROVIDER_MESSAGES;

	@Before
	public void setup() throws Exception {
		targetCache = new CFTargetCache(ImmutableList.of(cloudfoundry.paramsProvider), cloudfoundry.factory, timeouts);
	}

	@Test
	public void testNoTarget() throws Exception {

		when(cloudfoundry.paramsProvider.getParams())
				.thenThrow(new NoTargetsException(expectedMessages.getNoTargetsFound()));
		assertError(() -> targetCache.getOrCreate(), NoTargetsException.class, expectedMessages.getNoTargetsFound());
	}

	@Test
	public void testOneTarget() throws Exception {
		CFTarget target = targetCache.getOrCreate().get(0);
		assertNotNull(target);
		assertEquals(MockCfCli.DEFAULT_PARAMS, target.getParams());
	}

	@Test
	public void testUnknownHostServices() throws Exception {
		ClientRequests client = cloudfoundry.client;
		when(client.getServices()).thenThrow(new UnknownHostException("api.run.pivotal.io"));
		CFTarget target = targetCache.getOrCreate().get(0);
		assertError(() -> target.getServices(), ConnectionException.class, expectedMessages.getConnectionError());
	}

	@Test
	public void testUnknownHostBuildpacks() throws Exception {
		ClientRequests client = cloudfoundry.client;
		when(client.getBuildpacks()).thenThrow(new UnknownHostException("api.run.pivotal.io"));
		CFTarget target = targetCache.getOrCreate().get(0);
		assertError(() -> target.getBuildpacks(), ConnectionException.class, expectedMessages.getConnectionError());
	}
	
	@Test
	public void testUnknownHostDomains() throws Exception {
		ClientRequests client = cloudfoundry.client;
		when(client.getDomains()).thenThrow(new UnknownHostException("api.run.pivotal.io"));
		CFTarget target = targetCache.getOrCreate().get(0);
		assertError(() -> target.getDomains(), ConnectionException.class, expectedMessages.getConnectionError());
	}
	
	@Test
	public void testInvalidRefreshToken() throws Exception {
		ClientRequests client = cloudfoundry.client;
		String mockedError = "org.cloudfoundry.uaa.UaaException: invalid_token: Invalid refresh token expired at Wed Mar 28 18:58:20 UTC 2018";
		when(client.getDomains()).thenThrow(new UaaException(1111, mockedError, mockedError));
		CFTarget target = targetCache.getOrCreate().get(0);
		assertError(() -> target.getDomains(), ConnectionException.class, expectedMessages.getConnectionError());
	}

	@Test
	public void testBuildpacksFromTarget() throws Exception {
		ClientRequests client = cloudfoundry.client;
		CFBuildpack buildpack = Mockito.mock(CFBuildpack.class);
		when(buildpack.getName()).thenReturn("java_buildpack");
		when(client.getBuildpacks()).thenReturn(ImmutableList.of(buildpack));

		CFTarget target = targetCache.getOrCreate().get(0);
		assertEquals("java_buildpack", target.getBuildpacks().get(0).getName());
	}

	@Test
	public void testDomainsFromTarget() throws Exception {
		ClientRequests client = cloudfoundry.client;
		CFDomain domain = Mockito.mock(CFDomain.class);
		when(domain.getName()).thenReturn("cfapps.io");
		when(client.getDomains()).thenReturn(ImmutableList.of(domain));

		CFTarget target = targetCache.getOrCreate().get(0);
		assertEquals("cfapps.io", target.getDomains().get(0).getName());
	}

	@Test
	public void testServicesFromTarget() throws Exception {
		ClientRequests client = cloudfoundry.client;
		CFServiceInstance service = Mockito.mock(CFServiceInstance.class);
		when(service.getName()).thenReturn("appdb");
		when(service.getPlan()).thenReturn("spark");
		when(service.getService()).thenReturn("cleardb");

		when(client.getServices()).thenReturn(ImmutableList.of(service));

		CFTarget target = targetCache.getOrCreate().get(0);
		assertEquals("appdb", target.getServices().get(0).getName());
		assertEquals("spark", target.getServices().get(0).getPlan());
		assertEquals("cleardb", target.getServices().get(0).getService());
	}

	@Test
	public void testNoServicesFromTarget() throws Exception {
		ClientRequests client = cloudfoundry.client;

		when(client.getServices()).thenReturn(ImmutableList.of());

		CFTarget target = targetCache.getOrCreate().get(0);
		assertTrue(target.getServices().isEmpty());
	}

	protected void assertError(Callable<?> callable, Class<? extends Throwable> expected, String expectedMessage)
			throws Exception {
		Throwable error = null;

		try {
			callable.call();
		} catch (Exception e) {
			error = ExceptionUtil.getDeepestCause(e);
		}
		assertEquals(expected, error.getClass());
		assertTrue(ExceptionUtil.getMessageNoAppendedInformation(error).contains(expectedMessage));
	}

}
