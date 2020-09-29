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
package org.springframework.ide.eclipse.boot.dash.cf.client.v2;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Arguments passed to push operation.
 *
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class CFPushArguments implements AutoCloseable {
	private List<String> routes = ImmutableList.of();
	private String appName;
	private Integer memory;
	private Integer diskQuota;
	private Integer timeout;
	private String buildpack;
	private String command;
	private String stack;
	private String healthCheckType;
	private Map<String, String> env = ImmutableMap.of();
	private Integer instances;
	private List<String> services = ImmutableList.of();
	private File applicationDataAsFile;
	private boolean noStart = false;
	private boolean randomRoute = false;
	private String healthCheckHttpEndpoint;

	public CFPushArguments() {
	}

	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public Integer getMemory() {
		return memory;
	}
	public void setMemory(Integer memory) {
		this.memory = memory;
	}
	public Integer getDiskQuota() {
		return diskQuota;
	}
	public void setDiskQuota(Integer diskQuota) {
		this.diskQuota = diskQuota;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public String getBuildpack() {
		return buildpack;
	}
	public void setBuildpack(String buildpack) {
		this.buildpack = buildpack;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getStack() {
		return stack;
	}
	public void setStack(String stack) {
		this.stack = stack;
	}
	public Map<String, String> getEnv() {
		return env;
	}
	public void setEnv(Map<String, String> env) {
		this.env = env;
	}
	public Integer getInstances() {
		return instances;
	}
	public void setInstances(Integer instances) {
		this.instances = instances;
	}
	public List<String> getServices() {
		return services;
	}
	public void setServices(List<String> services) {
		this.services = services;
	}
	public boolean getRandomRoute() {
		return this.randomRoute;
	}
	public void setRandomRoute(boolean randomRoute) {
		this.randomRoute = randomRoute;
	}
	public File getApplicationDataAsFile() {
		return applicationDataAsFile;
	}
	public void setApplicationData(File archive) throws Exception {
		Assert.isLegal(this.applicationDataAsFile==null, "Can only set this once");
		this.applicationDataAsFile=archive;
	}
	public boolean isNoStart() {
		return noStart;
	}
	public void setNoStart(boolean noStart) {
		this.noStart = noStart;
	}
	public String getHealthCheckType() {
		if (healthCheckType==null) {
			return DeploymentProperties.DEFAULT_HEALTH_CHECK_TYPE;
		}
		return healthCheckType;
	}
	public void setHealthCheckType(String healthCheckType) {
		this.healthCheckType = healthCheckType;
	}
	public List<String> getRoutes() {
		return routes;
	}
	public void setRoutes(Collection<String> routes) {
		this.routes = routes == null ? ImmutableList.of() : ImmutableList.copyOf(routes);
	}
	public void setRoutes(String... routes) {
		setRoutes(ImmutableList.copyOf(routes));
	}
	@Override
	public String toString() {
		return "CFPushArguments [appName=" + appName + ", routes=" + routes + ", memory=" + memory + ", diskQuota="
				+ diskQuota + ", timeout=" + timeout + ", buildpack=" + buildpack + ", command=" + command + ", stack="
				+ stack + ", env=" + env + ", instances=" + instances + ", services=" + services + ", noStart="
				+ noStart + ", healthCheckType="+ healthCheckType+ ", randomRoute="+ randomRoute+" ]";
	}

	public void setHealthCheckHttpEndpoint(String healthCheckHttpEndpoint) {
		this.healthCheckHttpEndpoint = healthCheckHttpEndpoint;
	}

	public String getHealthCheckHttpEndpoint() {
		return this.healthCheckHttpEndpoint;
	}

	@Override
	public void close() throws Exception {
		//This used to do something more useful, we kept it for now to avoid having to
		// change a lot of code. But calling `close` is no longer needed and does nothing.
	}

}
