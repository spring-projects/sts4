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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.params.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.params.CFClientParamsFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

public class CFClientTest {

	@Ignore @Test public void testGetBuildpacksFromCliParams() throws Exception {

		CFClientParams params = CFClientParamsFactory.INSTANCE.getParams().get(0);
		assertNotNull(params);

		ClientRequests requests = DefaultCloudFoundryClientFactoryV2.INSTANCE.getClient(params);

		assertNotNull(requests);

		List<CFBuildpack> buildPacks = requests.getBuildpacks();
		assertTrue(!buildPacks.isEmpty());

	}

}
