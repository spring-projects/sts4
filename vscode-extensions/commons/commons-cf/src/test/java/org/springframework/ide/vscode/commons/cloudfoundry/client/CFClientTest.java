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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParamsFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargets;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

public class CFClientTest {


	
	@Ignore @Test public void testGetBuildpacksFromCliParamsTarget() throws Exception {

		CFClientParamsFactory paramsFactory = CFClientParamsFactory.INSTANCE;
		CloudFoundryClientFactory clientFactory = DefaultCloudFoundryClientFactoryV2.INSTANCE;

		CFTargets targets = new CFTargets(paramsFactory, clientFactory);
		CFTarget target = targets.getTargets().get(0);

		List<CFBuildpack> buildPacks = target.getBuildpacks();
		assertTrue(!buildPacks.isEmpty());
	}

}
