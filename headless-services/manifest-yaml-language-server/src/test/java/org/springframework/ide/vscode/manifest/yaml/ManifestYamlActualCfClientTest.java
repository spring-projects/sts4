/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.manifest.yaml;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTarget;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFTargetCache;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfo;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CfTargetsInfoProvder;
import org.springframework.ide.vscode.commons.cloudfoundry.client.v2.DefaultCloudFoundryClientFactoryV2;

import com.google.common.collect.ImmutableList;

public class ManifestYamlActualCfClientTest {

	private CFTargetCache cfTargetCache;
	private CfJson cfJson;

	@Before
	public void setup() throws Exception {
		cfJson = new CfJson();
		CfTargetsInfo info = getTargetsInfoFromEnv();
		CfTargetsInfoProvder provider = new CfTargetsInfoProvder(info);
		CloudFoundryClientFactory clientFactory = DefaultCloudFoundryClientFactoryV2.INSTANCE;
		cfTargetCache = new CFTargetCache(ImmutableList.of(provider), clientFactory, new ClientTimeouts());
	}

	private CfTargetsInfo getTargetsInfoFromEnv() {
		CfTestTargetParams testParams = CfTestTargetParams.fromEnv();
		String rawJson = "{\n" + "	\"cfTargets\": [\n" + "		{\n"
				+ "			\"api\":\"" + testParams.api + "\",\n"
				+ "			\"org\":\"" + testParams.org + "\",\n"
				+ "			\"space\": \"" + testParams.space + "\",\n"
				+ "			\"refreshToken\": \"" +  testParams.credentials.getSecret() + "\",\n"
				+ "			\"sslDisabled\": false\n"
				+ "		}\n" + "	],\n" + "	\"cfDiagnosticMessages\": {\n"
				+ "		\"noTargetsFound\": \"No Cloud Foundry targets found: Connect CF Target(s) in Boot Dashboard or login via CF CLI\",\n"
				+ "		\"unauthorised\": \"Permission denied: Verify credentials to CF Target from Boot Dashboard or CF CLI are correct\",\n"
				+ "		\"noNetworkConnection\": \"No connection to Cloud Foundry: Connect CF Target via Boot Dashboard or login via CF CLI or verify network connections\",\n"
				+ "		\"noOrgSpace\": \"No org/space selected: Connect CF Target in Boot Dashboard or login via CF CLI\"\n"
				+ "	}\n" + "}";
		return cfJson.from(rawJson);
	}

	@Ignore @Test
	public void testGetBuildpacks() throws Exception {
		List<CFTarget> targets = cfTargetCache.getOrCreate();
		assertTrue(targets.size() == 1);
		CFTarget target = targets.get(0);
		List<CFBuildpack> buildpacks = target.getBuildpacks();
		assertTrue(!buildpacks.isEmpty());
	}
}
