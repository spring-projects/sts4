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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFEntities;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientRequests;
import org.springframework.ide.vscode.commons.cloudfoundry.client.ClientTimeouts;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFClientParams;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.CFCredentials;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.ClientParamsProvider;
import org.springframework.ide.vscode.commons.cloudfoundry.client.cftarget.NoTargetsException;

import com.google.common.collect.ImmutableList;

/**
 * An alternative to using mockito for mocking the CF client, as the mockito
 * version of the CF client seems to fail when run on OpenJDK (which is used for
 * the sts4 concourse ci build).
 * <p/>
 * This also allows additional testing of the general manifest-yaml vscode
 * framework, as the framework should be able to take any client factory,
 * including the basic one below, and still produce correct results for content
 * assist as well as reconcile
 *
 */
public class BasicCfClientHarness {

	private BasicCFClientFactory clientFactory = new BasicCFClientFactory();

	private ClientParamsProvider paramsProvider = new BasicClientParamsProvider(ImmutableList.of(DEFAULT_PARAMS));

	public static CFClientParams DEFAULT_PARAMS = new CFClientParams("test.io", "testuser",
			CFCredentials.fromRefreshToken("refreshtoken"), false);

	public BasicCFClientFactory getBasicClientFactory() {
		return clientFactory;
	}

	public ClientParamsProvider getParamsProvider() {
		return paramsProvider;
	}

	public void addBuildpacks(String... buildpacks) {

		// Allow testing of null condition when vscode asks for buildpacks from
		// the client and
		// client returns null instead of empty list
		List<CFBuildpack> asList = null;
		if (buildpacks != null) {
			asList = new ArrayList<CFBuildpack>();
			for (String name : buildpacks) {
				asList.add(CFEntities.createBuildpack(name));
			}
		}

		getBasicClientFactory().getExistingClientInHarness().setBuildPacks(asList);
	}

	public static final class BasicCFClientFactory implements CloudFoundryClientFactory {

		/*
		 * Create the client "ahead of time" so that it can be configured before
		 * the language server is tested
		 */
		private BasicClientRequests preexistingClient = new BasicClientRequests(DEFAULT_PARAMS, new ClientTimeouts());

		@Override
		public ClientRequests getClient(CFClientParams params, ClientTimeouts timeouts) throws Exception {
			return this.preexistingClient;
		}

		/**
		 * Convenient non-framework method to fetch the existing client so that
		 * values can be set to simulate values from CF.
		 * 
		 * @return
		 */
		public BasicClientRequests getExistingClientInHarness() {
			return this.preexistingClient;
		}
	}

	static class BasicClientParamsProvider implements ClientParamsProvider {

		private List<CFClientParams> params;

		public BasicClientParamsProvider(List<CFClientParams> defaultParams) {
			this.params = defaultParams;
		}

		public void setParams(List<CFClientParams> params) {
			this.params = params;
		}

		@Override
		public List<CFClientParams> getParams() throws NoTargetsException, ExecutionException {
			return this.params;
		}

	}

	static class BasicClientRequests implements ClientRequests {

		private List<CFBuildpack> buildpacks;
		private List<CFServiceInstance> serviceInstances;
		private CFClientParams params;
		private ClientTimeouts timeouts;

		public BasicClientRequests(CFClientParams params, ClientTimeouts timeouts) {
			this.params = params;
			this.timeouts = timeouts;
		}

		@Override
		public List<CFBuildpack> getBuildpacks() throws Exception {
			return this.buildpacks;
		}

		@Override
		public List<CFServiceInstance> getServices() throws Exception {
			return this.serviceInstances;
		}

		public void setBuildPacks(List<CFBuildpack> buildpacks) {
			this.buildpacks = buildpacks;
		}

		public void setServices(List<CFServiceInstance> serviceInstances) {
			this.serviceInstances = serviceInstances;
		}
	}

}
