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
package org.springframework.ide.vscode.bosh.mocks;

import java.util.concurrent.Callable;

import org.springframework.ide.vscode.bosh.BoshCommandCloudConfigProviderTest;
import org.springframework.ide.vscode.bosh.cloudconfig.BoshCommandCloudConfigProvider;
import org.springframework.ide.vscode.commons.util.IOUtil;

public final class MockCloudConfigProvider extends BoshCommandCloudConfigProvider {

	static final String MOCK_DATA_RSRC = "/cmd-out/cloud-config.json";

	private Callable<String> cloudConfigReader = () -> IOUtil.toString(BoshCommandCloudConfigProviderTest.class.getResourceAsStream(MOCK_DATA_RSRC));

	/**
	 * Override with a 'fake' which just returns some mock data. That way we can unit-test
	 * without requiring a real bosh setup.
	 */
	@Override
	protected String executeBoshCloudConfigCommand() throws Exception {
		return cloudConfigReader.call();
	}

	public MockCloudConfigProvider readWith(Callable<String> reader) {
		this.cloudConfigReader = reader;
		return this;
	}
}