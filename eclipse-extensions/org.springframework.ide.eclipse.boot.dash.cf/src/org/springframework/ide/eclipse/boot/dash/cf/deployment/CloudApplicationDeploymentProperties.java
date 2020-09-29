/*******************************************************************************
 * Copyright (c) 2015, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFRoute;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;

import com.google.common.collect.ImmutableList;

public class CloudApplicationDeploymentProperties implements DeploymentProperties {

	private boolean enableJmxSshTunnel;
	private List<String> boundServices;
	private Map<String, String> environmentVariables;
	private String buildpack;
	private List<String> buildpacks;

	private int instances;

	/*
	 * URLs should never be null. If no URLs are needed, keep list empty
	 */
	private LinkedHashSet<String> urls;

	private String appName;

	public boolean getEnableJmxSshTunnel() {
		return enableJmxSshTunnel;
	}

	public void setEnableJmxSshTunnel(boolean enableJmxSshTunnel) {
		this.enableJmxSshTunnel = enableJmxSshTunnel;
	}

	private IProject project;

	private int memory;

	private int diskQuota;

	private IFile manifestFile;

	private Integer timeout;

	private String command;

	private String stack;

	/**
	 * Path to a zipFile containing the contents of the stuff to deploy.
	 */
	private File archive;
	private String healthCheckType;
	private String healthCheckHttpEndpoint;
	private boolean randomeRoute = false;

	public CloudApplicationDeploymentProperties() {
		boundServices = new ArrayList<>();
		environmentVariables = new HashMap<>();
		buildpack = "";
		buildpacks = new ArrayList<>();
		instances = DeploymentProperties.DEFAULT_INSTANCES;
		urls = new LinkedHashSet<>();
		appName = null;
		project = null;
		memory = DeploymentProperties.DEFAULT_MEMORY;
		diskQuota = DeploymentProperties.DEFAULT_MEMORY;
		manifestFile = null;
		timeout = null;
		command = null;
		stack = null;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int getMemory() {
		return memory;
	}

	public void setDiskQuota(int diskQuota) {
		this.diskQuota = diskQuota;
	}

	public int getDiskQuota() {
		return diskQuota;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public String getHealthCheckType() {
		return healthCheckType;
	}

	public void setHealthCheckType(String healthCheckType) {
		this.healthCheckType = healthCheckType;
	}

	public String getHealthCheckHttpEndpoint() {
		return healthCheckHttpEndpoint;
	}

	public void setHealthCheckHttpEndpoint(String hche) {
		this.healthCheckHttpEndpoint = hche;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public String getStack() {
		return stack;
	}

	public void setManifestFile(IFile file) {
		this.manifestFile = file;
	}

	public IFile getManifestFile() {
		return this.manifestFile;
	}

	/**
	 * Returns a copy of the list of URLs for the application
	 *
	 * @return never null
	 */
	public Set<String> getUris() {
		return urls;
	}

	public void setUris(Collection<String> urls) {
		this.urls = urls == null ? new LinkedHashSet<>() : new LinkedHashSet<>(urls);
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return this.appName;
	}

	public void setBuildpack(String buildpack) {
		this.buildpack = buildpack;
	}

	public void setServices(List<String> services) {
		/*
		 * List should be read/write accessible hence create new instance rather
		 * than use emptyList from Collections if null is passed in
		 */
		boundServices = services == null ? new ArrayList<>() : services;
	}

	public void setBuildpacks(List<String> buildpacks) {
		this.buildpacks = buildpacks == null ? new ArrayList<>() : buildpacks;
	}

	public void setInstances(int instances) {
		this.instances = instances;
	}

	public void setRandomRoute(boolean randomRoute) {
		this.randomeRoute = randomRoute;
	}

	public boolean getRandomRoute() {
		return this.randomeRoute ;
	}

	public void setEnvironmentVariables(Map<String, String> environmentVariables) {
		/*
		 * Map should be read/write accessible hence create new instance rather
		 * than use emptyMap from Collections if null is passed in
		 */
		this.environmentVariables = environmentVariables == null ? new HashMap<>() : environmentVariables;
	}

	public String getBuildpack() {
		return buildpack == null || buildpack.isEmpty() ? null : buildpack;
	}

	@Override
	public List<String> getBuildpacks() {
		return ImmutableList.copyOf(buildpacks);
	}

	public int getInstances() {
		return instances;
	}

	/**
	 * @return never null
	 */
	public Map<String, String> getEnvironmentVariables() {
		return environmentVariables;
	}

	/**
	 *
	 * @return never null
	 */
	public List<String> getServices() {
		return boundServices;
	}

	public static CloudApplicationDeploymentProperties getFor(IProject project, CloudData cloudData, CFApplication app) {

		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties();

		properties.setAppName(app == null ? project.getName() : app.getName());
		properties.setProject(project);
		properties.setBuildpack(app == null ? cloudData.getBuildpack() : app.getBuildpackUrl());

		/*
		 * TODO: Re-evaluate whether JAVA_OPTS need to be treated differently
		 * Boot Dash Tooling adds staff to JAVA-OPTS behind the scenes. Consider
		 * JAVA_OPTS env variable as the one not exposed to users
		 */
		Map<String, String> env = new LinkedHashMap<>();
		if (app != null) {
			env.putAll(app.getEnvAsMap());
			env.remove("JAVA_OPTS");
		}
		
		// PT 174076433 - Support deploying of Java 11 apps to CF
		if (JavaProjectUtil.isJava11(project)) {
			env.put("JBP_CONFIG_OPEN_JDK_JRE", "{ jre: { version: 11.+}}");
		}

		properties.setEnvironmentVariables(env);

		properties.setInstances(app == null ? 1 : app.getInstances());
		properties.setMemory(app == null ? DeploymentProperties.DEFAULT_MEMORY : app.getMemory());
		properties.setServices(app == null ? Collections.<String>emptyList() : app.getServices());
		properties.setDiskQuota(app == null ? DeploymentProperties.DEFAULT_MEMORY : app.getDiskQuota());
		properties.setTimeout(app == null ? null : app.getTimeout());
		properties.setHealthCheckType(app==null ? null : app.getHealthCheckType());
		properties.setHealthCheckHttpEndpoint(app == null ? null : app.getHealthCheckHttpEndpoint());
		properties.setCommand(app == null ? null : app.getCommand());
		properties.setStack(app == null ? null : app.getStack());

		if (app == null) {
			CFRoute route = CFRoute.builder().host(project.getName()).domain(cloudData.getDefaultDomain()).build();
			properties.setUris(Collections.singletonList(route.getRoute()));
		} else {
			properties.setUris(app.getUris());
		}
		return properties;
	}

	public CFPushArguments toPushArguments(List<CFCloudDomain> cloudDomains) throws Exception {
		Set<String> uris = getUris();
		CFPushArguments args = new CFPushArguments();
		args.setRoutes(uris);
		args.setAppName(getAppName());
		args.setMemory(getMemory());
		args.setDiskQuota(getDiskQuota());
		args.setTimeout(getTimeout());
		args.setHealthCheckType(getHealthCheckType());
		args.setHealthCheckHttpEndpoint(getHealthCheckHttpEndpoint());
		args.setBuildpack(getBuildpack());
		args.setCommand(getCommand());
		args.setStack(getStack());
		args.setEnv(getEnvironmentVariables());
		args.setInstances(getInstances());
		args.setServices(getServices());
		args.setApplicationData(getArchive());
		args.setRandomRoute(getRandomRoute());
		return args;
	}

	public File getArchive() {
		return archive;
	}

	public void setArchive(File archive) {
		this.archive = archive;
	}
}
