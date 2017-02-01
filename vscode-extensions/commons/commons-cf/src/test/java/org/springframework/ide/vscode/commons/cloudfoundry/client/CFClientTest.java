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
package org.springframework.ide.vscode.commons.cloudfoundry.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.net.UnknownHostException;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFParamsProviderMessages;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliProviderMessages;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ConnectionException;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.NoTargetsException;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

public class CFClientTest {

	MockCfCli cloudfoundry = new MockCfCli();
	ClientTimeouts timeouts = new ClientTimeouts();
	CFTargetCache targetCache;
	CFParamsProviderMessages expectedMessages = new CfCliProviderMessages();

	@Before
	public void setup() throws Exception {
		targetCache = new CFTargetCache(cloudfoundry.paramsProvider, cloudfoundry.factory, timeouts);
	}

	@Test
	public void testNoTarget() throws Exception {

		when(cloudfoundry.paramsProvider.getParams())
				.thenThrow(new NoTargetsException(expectedMessages.noTargetsFound()));
		assertError(() -> targetCache.getOrCreate(), NoTargetsException.class, expectedMessages.noTargetsFound());
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
		assertError(() -> target.getServices(), ConnectionException.class, expectedMessages.noNetworkConnection());
	}

	@Test
	public void testUnknownHostBuildpacks() throws Exception {
		ClientRequests client = cloudfoundry.client;
		when(client.getBuildpacks()).thenThrow(new UnknownHostException("api.run.pivotal.io"));
		CFTarget target = targetCache.getOrCreate().get(0);
		assertError(() -> target.getBuildpacks(), ConnectionException.class, expectedMessages.noNetworkConnection());
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
