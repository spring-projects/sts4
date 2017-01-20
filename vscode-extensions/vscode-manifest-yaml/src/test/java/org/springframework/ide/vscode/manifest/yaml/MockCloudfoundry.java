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
package org.springframework.ide.vscode.manifest.yaml;

import org.mockito.Mockito;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;

public class MockCloudfoundry implements CloudFoundryClientFactory {

	public final ClientRequests client = Mockito.mock(ClientRequests.class);
	private Exception toThrow = null;

	@Override
	public ClientRequests getClient(CFClientParams params, ClientTimeouts timeouts) throws Exception {
		if (toThrow==null) {
			return client;
		}
		throw toThrow;
	}

	/**
	 * Makes the factory throw the given exception when its 'getClient' method is called.
	 */
	public void throwException(Exception e) {
		toThrow = e;
	}

}
