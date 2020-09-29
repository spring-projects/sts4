/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.mockito.Mockito;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFOrganization;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFServiceInstance;
import org.springsource.ide.eclipse.commons.livexp.core.LiveCounter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class MockCFSpace extends CFSpaceData {

	//TODO: the methods in this class should prolly be synchronized somehow. It manipulates mutable
	//  data and is called from multiple threads.

	private Map<String, CFServiceInstance> servicesByName = new HashMap<>();
	private Map<String, MockCFApplication> appsByName = new HashMap<>();
	private MockCloudFoundryClientFactory owner;
	private Map<String, LiveCounter> pushCounts = new HashMap<>();

	public MockCFSpace(MockCloudFoundryClientFactory owner, String name, UUID guid, CFOrganization org) {
		super(name, guid, org);
		this.owner = owner;
	}

	public String getDefaultDomain() {
		return owner.getDefaultDomain();
	}

	public List<CFServiceInstance> getServices() {
		return ImmutableList.copyOf(servicesByName.values());
	}

	public ImmutableList<CFApplication> getApplicationsWithBasicInfo() {
		Builder<CFApplication> builder = ImmutableList.builder();
		for (MockCFApplication app : appsByName.values()) {
			builder.add(app.getBasicInfo());
		}
		return builder.build();
	}

	public MockCFApplication defApp(String name) {
		MockCFApplication existing = appsByName.get(name);
		if (existing==null) {
			appsByName.put(name, existing = Mockito.spy(new MockCFApplication(owner, this, name)));
		}
		return existing;
	}

	public CFServiceInstance defService(String name) {
		CFServiceInstance existing = servicesByName.get(name);
		if (existing==null) {
			servicesByName.put(name, new CFServiceInstanceData(
					name
			));
		}
		return existing;
	}

	public MockCFApplication getApplication(UUID appGuid) {
		for (MockCFApplication app : appsByName.values()) {
			if (app.getGuid().equals(appGuid)) {
				return app;
			}
		}
		return null;
	}

	public MockCFApplication getApplication(String appName) {
		MockCFApplication app = appsByName.get(appName);
		if (app!=null) {
			return app;
		}
		return null;
	}


	public boolean removeApp(String name) {
		return appsByName.remove(name)!=null;
	}

	public void put(MockCFApplication app) {
		appsByName.put(app.getName(), app);
	}

	public synchronized LiveCounter getPushCount(String name) {
		LiveCounter counter = pushCounts.get(name);
		if (counter==null) {
			pushCounts.put(name, counter = new LiveCounter());
		}
		return counter;
	}

	public void deleteService(String serviceName) throws Exception {
		CFServiceInstance x = servicesByName.remove(serviceName);
		if (x==null) {
			throw new IOException("Service doesn't exist: "+serviceName);
		}
	}

	@Override
	public String toString() {
		return "MockCFSpace("+getName()+")";
	}
}
