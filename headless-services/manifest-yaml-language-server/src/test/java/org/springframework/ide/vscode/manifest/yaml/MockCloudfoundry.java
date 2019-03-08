/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFCredentials;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfCliParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

public class MockCloudfoundry {

	public final CFClientParams DEFAULT_PARAMS = new CFClientParams("test.io", "testuser",
			CFCredentials.fromRefreshToken("refreshtoken"), "an-org", "a-space", false);

	public final CloudFoundryClientFactory factory = mock(CloudFoundryClientFactory.class);
	public final ClientRequests client = mock(ClientRequests.class);
	public final ClientParamsProvider defaultParamsProvider = mock(ClientParamsProvider.class);

	public MockCloudfoundry() {
		try {

			//program some default behavior into mocks... most tests will use this.
			//other tests should 'reset' the mocks and reprogram them as needed.
			when(factory.getClient(any(), any())).thenReturn(client);
			when(defaultParamsProvider.getParams()).thenReturn(ImmutableList.of(DEFAULT_PARAMS));
			when(defaultParamsProvider.getMessages()).thenReturn(CfCliParamsProvider.getInstance().getMessages());
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * Reset the mocks. Use this if the default's programmed into the mocks don't suite your test case.
	 * <p>
	 * Note: you may also choose to call {@link Mockito}.reset directly if you do not want to
	 * reset all of the mocks.
	 */
	public void reset() throws Exception {
		Mockito.reset(factory, client, defaultParamsProvider);
	}

}
