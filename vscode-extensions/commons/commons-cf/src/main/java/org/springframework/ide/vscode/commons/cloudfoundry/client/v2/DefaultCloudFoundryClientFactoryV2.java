/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.cloudfoundry.client.v2;

import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;

public class DefaultCloudFoundryClientFactoryV2 implements CloudFoundryClientFactory {

	public static final CloudFoundryClientFactory INSTANCE = new DefaultCloudFoundryClientFactoryV2();

	/**
	 * Use 'INSTANCE' constant instead. This class is a singleton.
	 */
	private DefaultCloudFoundryClientFactoryV2() {}

	private CloudFoundryClientCache cache = new CloudFoundryClientCache();

	/* (non-Javadoc)
	 * @see org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory#getClient(org.springframework.ide.vscode.commons.cloudfoundry.client.target.CFClientParams)
	 */
	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {
		return new DefaultClientRequestsV2(cache, params);
	}
}
