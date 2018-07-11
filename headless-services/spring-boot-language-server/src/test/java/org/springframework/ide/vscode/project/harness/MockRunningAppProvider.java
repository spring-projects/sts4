/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.project.harness;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.mockito.Mockito;
import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMapping;
import org.springframework.ide.vscode.commons.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

public class MockRunningAppProvider {

	public final RunningAppProvider provider = mock(RunningAppProvider.class);
	public final Collection<SpringBootApp> mockedApps = new ArrayList<>();

	public MockRunningAppProvider() {
		try {
			when(provider.getAllRunningSpringApps(anyObject())).thenReturn(mockedApps);
		} catch (Exception e) {
			throw ExceptionUtil.unchecked(e);
		}
	}

	/**
	 * Reset the mocks. Use this if the default's programmed into the mocks don't
	 * suite your test case.
	 * <p>
	 * Note: you may also choose to call {@link Mockito}.mock directly if you do not
	 * want to reset all of the mocks.
	 */
	public void reset() throws Exception {
		mockedApps.clear();
		Mockito.reset(provider);
	}

	public MockAppBuilder builder() {
		return new MockAppBuilder(this);
	}

	public static class MockAppBuilder {
		public final SpringBootApp app = mock(SpringBootApp.class);
		private MockRunningAppProvider runningAppProvider;
		private String processId;
		private String processName;

		public MockAppBuilder(MockRunningAppProvider runningAppProvider) {
			this.runningAppProvider = runningAppProvider;
		}

		public MockAppBuilder enviroment(String env) throws Exception {
			when(app.getEnvironment()).thenReturn(env);
			return this;
		}

		public MockAppBuilder beans(String beans) throws Exception {
			return beans(LiveBeansModel.parse(beans));
		}

		public MockAppBuilder beans(LiveBeansModel beans) {
			when(app.getBeans()).thenReturn(beans);
			return this;
		}

		public MockAppBuilder processId(String processId) {
			this.processId = processId;
			when(app.getProcessID()).thenReturn(processId);
			return this;
		}

		public MockAppBuilder processName(String name) {
			this.processName = name;
			when(app.getProcessName()).thenReturn(name);
			return this;
		}

		public MockAppBuilder port(String port) throws Exception {
			when(app.getPort()).thenReturn(port);
			return this;
		}

		public MockAppBuilder host(String host) throws Exception {
			when(app.getHost()).thenReturn(host);
			return this;
		}

		public MockAppBuilder isSpringBootApp(boolean isBoot) throws Exception {
			when(app.isSpringBootApp()).thenReturn(isBoot);
			return this;
		}

		public MockAppBuilder requestMappings(String mappings) throws Exception {
			Collection<RequestMapping> requestMappings = SpringBootApp.parseRequestMappingsJson(mappings, "1.x");
			when(app.getRequestMappings()).thenReturn(requestMappings);
			return this;
		}

		public MockAppBuilder liveConditionalsJson(String rawJson) throws Exception{
			when(app.getLiveConditionals()).thenReturn(SpringBootApp.getLiveConditionals(rawJson, processId, processName));
			return this;
		}

		public MockAppBuilder profiles(String... names) {
			when(app.getActiveProfiles()).thenReturn(ImmutableList.copyOf(names));
			return this;
		}

		public MockAppBuilder profilesUnknown() {
			//Note, technically, we don't have to program the mock for this case as it will return
			// null by default. But it makes test code more readable. Also... how we represent the
			// 'unknown' case may change in the future and having this method will help fix the tests.
			when(app.getActiveProfiles()).thenReturn(null);
			return this;
		}


		/**
		 * Builds the mock app and adds it to the app provider
		 */
		public void build() {
			runningAppProvider.mockedApps.add(app);
		}

	}
}
