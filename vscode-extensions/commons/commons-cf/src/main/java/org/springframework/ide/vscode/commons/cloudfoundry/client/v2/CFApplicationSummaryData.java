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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ide.vscode.commons.cloudfoundry.client.CFAppState;
import org.springframework.ide.vscode.commons.cloudfoundry.client.CFApplication;

public class CFApplicationSummaryData implements CFApplication {

	private String name;
	private int instances;
	private int runningInstances;
	private int memory;
	private UUID guid;
	private List<String> uris;
	private CFAppState state;
	private int diskQuota;
	protected ApplicationExtras extras;

	public CFApplicationSummaryData(
			String name,
			int instances,
			int runningInstances,
			int memory,
			UUID guid,
			List<String> uris,
			CFAppState state,
			int diskQuota,
			ApplicationExtras extras
	) {
		super();
		this.name = name;
		this.instances = instances;
		this.runningInstances = runningInstances;
		this.memory = memory;
		this.guid = guid;
		this.uris = uris;
		this.state = state;
		this.diskQuota = diskQuota;
		this.extras = extras;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getInstances() {
		return instances;
	}

	@Override
	public int getRunningInstances() {
		return runningInstances;
	}

	@Override
	public int getMemory() {
		return memory;
	}

	@Override
	public UUID getGuid() {
		return guid;
	}

	@Override
	public List<String> getServices() {
		return extras.getServices().block();
	}

	@Override
	public String getBuildpackUrl() {
		return extras.getBuildpack().block();
	}

	@Override
	public List<String> getUris() {
		return uris;
	}

	@Override
	public CFAppState getState() {
		return state;
	}

	@Override
	public int getDiskQuota() {
		return diskQuota;
	}

	@Override
	public Integer getTimeout() {
		return extras.getTimeout().block();
	}

	@Override
	public String getHealthCheckType() {
		return extras.getHealthCheckType().block();
	}

	@Override
	public String getCommand() {
		return extras.getCommand().block();
	}

	@Override
	public String getStack() {
		return extras.getStack().block();
	}

	@Override
	public Map<String, String> getEnvAsMap() {
		return extras.getEnv().block();
	}

}
