/*******************************************************************************
 * Copyright (c) 2015, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.deployment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFDomainType;
import org.springframework.ide.eclipse.boot.dash.cf.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFDomainStatus;
import org.springframework.ide.eclipse.boot.dash.cf.client.v2.CFRoute;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.collect.ImmutableList;

/**
 * Reads and creates manifest.yml content from a specific location relative to
 * an {@link IProject}.
 *
 */
public class ApplicationManifestHandler {

	public static final String RANDOM_VAR = "${random}"; //$NON-NLS-1$

	public static final String APPLICATIONS_PROP = "applications";

	public static final String NAME_PROP = "name";

	public static final String MEMORY_PROP = "memory";

	public static final String INSTANCES_PROP = "instances";

	public static final String SUB_DOMAIN_PROP = "host";

	public static final String SUB_DOMAINS_PROP = "hosts";

	public static final String DOMAIN_PROP = "domain";

	public static final String DOMAINS_PROP = "domains";

	public static final String NO_ROUTE_PROP = "no-route";

	public static final String NO_HOSTNAME_PROP = "no-hostname";

	public static final String RANDOM_ROUTE_PROP = "random-route";

	public static final String SERVICES_PROP = "services";

	public static final String LABEL_PROP = "label";

	public static final String PROVIDER_PROP = "provider";

	public static final String VERSION_PROP = "version";

	public static final String PLAN_PROP = "plan";

	public static final String PATH_PROP = "path";

	public static final String BUILDPACK_PROP = "buildpack";

	public static final String BUILDPACKS_PROP = "buildpacks";

	public static final String ENV_PROP = "env";

	public static final String DISK_QUOTA_PROP = "disk_quota";

	public static final String INHERIT_PROP = "inherit";

	public static final String TIMEOUT_PROP = "timeout";

	public static final String HEALTH_CHECK_TYPE_PROP = "health-check-type";

	public static final String HEALTH_CHECK_HTTP_ENDPOINT_PROP = "health-check-http-endpoint";

	public static final String COMMAND_PROP = "command";

	public static final String STACK_PROP = "stack";

	public static final String ROUTES_PROP = "routes";

	public static final String ROUTE_PROP = "route";

	private final IProject project;

	private final IFile manifestFile;

	private final CloudData cloudData;

	public ApplicationManifestHandler(IProject project, CloudData cloudData) {
		this(project, cloudData, null);
	}

	public ApplicationManifestHandler(IProject project, CloudData cloudData, IFile manifestFile) {
		this.project = project;
		this.manifestFile = manifestFile;
		this.cloudData = cloudData;
	}

	protected InputStream getInputStream() throws Exception {

		File file = getManifestFile();
		if (file != null && file.exists()) {
			return new FileInputStream(file);
		} else {
			throw ExceptionUtil.coreException("No manifest.yml file found in project: " + project.getName());
		}
	}

	/**
	 *
	 * @return manifest file if it exists. Null otherwise
	 */
	public File getManifestFile() {
		if (manifestFile != null) {
			URI locationURI = manifestFile.getLocationURI();
			if (locationURI != null) {
				File file = new File(locationURI);
				return file.exists() ? file : null;
			}
		}
		return null;
	}

	public boolean hasManifest() {
		File file = getManifestFile();
		return file != null && file.exists();
	}

	/**
	 *
	 * @param applicationName
	 *            name of application to lookup in the manifest file.
	 * @param propertyName
	 *            String value property to retrieve from manifest for given
	 *            application entry.
	 * @return Value of property, or null if not found, or entry for application
	 *         in manifest does not exist.
	 */
	public String getApplicationProperty(String appName, String propertyName, IProgressMonitor monitor) {
		try {
			Map<?, ?> appMap = getApplicationMap(appName, monitor);
			if (appMap != null) {
				return getValue(appMap, propertyName, String.class);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public Map<?, ?> getApplicationMap(String appName, IProgressMonitor monitor) throws Exception {
		Map<Object, Object> allResults = parseManifestFromFile();

		List<Map<?, ?>> appMaps = getApplications(allResults);

		for (Map<?, ?> appMap : appMaps) {

			String existingAppName = getValue(appMap, NAME_PROP, String.class);
			if (existingAppName != null && existingAppName.equals(appName)) {
				return appMap;
			}
		}
		return null;
	}

	/**
	 *
	 * @param containerMap
	 * @param propertyName
	 * @return map of values for the given property name, or null if it cannot
	 *         be resolved
	 */
	@SuppressWarnings("unchecked")
	protected Map<?, ?> getContainingPropertiesMap(Map<?, ?> containerMap, String propertyName) {
		if (containerMap == null || propertyName == null) {
			return null;
		}
		Object yamlElementObj = containerMap.get(propertyName);

		if (yamlElementObj instanceof Map) {
			return (Map<Object, Object>) yamlElementObj;
		} else {
			return null;
		}
	}

//	protected String getStringValue(Map<?, ?> containingMap, String propertyName) {
//
//		if (containingMap == null) {
//			return null;
//		}
//
//		Object valObj = containingMap.get(propertyName);
//
//		if (valObj instanceof String) {
//			return (String) valObj;
//		}
//		return null;
//	}
//
	@SuppressWarnings("unchecked")
	protected static <T> T getValue(Map<?, ?> containingMap, String propertyName, Class<T> type) {
		if (containingMap == null) {
			return null;
		}
		Object valObj = containingMap.get(propertyName);

		if (valObj != null && type.isAssignableFrom(valObj.getClass())) {
			return (T) valObj;
		}
		return null;
	}

	protected static boolean hasValue(Map<?, ?> containingMap, String propertyName) {
		return containingMap != null && containingMap.containsKey(propertyName);
	}

	public static List<Map<?, ?>> getApplications(Map<?, ?> results) throws CoreException {

		Object applicationsObj = results.get(APPLICATIONS_PROP);
		if (!(applicationsObj instanceof List<?>)) {
			return null;
		}

		List<?> applicationsList = (List<?>) applicationsObj;

		List<Map<?, ?>> applications = new ArrayList<>();

		// Use only the first application entry
		if (!applicationsList.isEmpty()) {
			for (Object val : applicationsList) {
				if (val instanceof Map<?, ?>) {
					applications.add((Map<?, ?>) val);
				}

			}
		}

		return applications;
	}

	protected CloudApplicationDeploymentProperties getDeploymentProperties(Map<?, ?> appMap,
			Map<Object, Object> allResults, IProgressMonitor monitor) throws Exception {

		CloudApplicationDeploymentProperties properties = new CloudApplicationDeploymentProperties();

		String appName = getValue(appMap, NAME_PROP, String.class);

		properties.setAppName(appName);
		properties.setProject(project);
		properties.setManifestFile(manifestFile);

		readMemory(appMap, allResults, properties);

		readDiskQuota(appMap, allResults, properties);

		readApplicationURL(appMap, allResults, properties);

		readBuildpack(appMap, allResults, properties);

		readBuildpacks(appMap, allResults, properties);

		readEnvVars(appMap, allResults, properties);

		readServices(appMap, allResults, properties);

		readInstances(appMap, allResults, properties);

		readTimeout(appMap, allResults, properties);

		readHealthCheckType(appMap, allResults, properties);

		readHealthCheckHttpEndpoint(appMap, allResults, properties);

		readCommand(appMap, allResults, properties);

		readStack(appMap, allResults, properties);

		return properties;
	}

	public List<CloudApplicationDeploymentProperties> load(IProgressMonitor monitor) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask("Loading manifest.yml", 6);

		try {

			Map<Object, Object> allResults = parseManifestFromFile();

			if (allResults == null || allResults.isEmpty()) {
				throw ExceptionUtil
						.coreException("No content found in manifest.yml. Make sure the manifest is valid.");

			}

			List<Map<?, ?>> appMaps = getApplications(allResults);

			List<CloudApplicationDeploymentProperties> properties = new ArrayList<>();

			if (appMaps == null) {
				CloudApplicationDeploymentProperties props = getDeploymentProperties(allResults, allResults, subMonitor);
				if (props != null) {
					properties.add(props);
				}
			} else {
				for (Map<?, ?> app : appMaps) {
					CloudApplicationDeploymentProperties props = getDeploymentProperties(app, allResults, subMonitor);
					if (props != null) {
						properties.add(props);
					}
				}
			}

			return properties;

		} finally {
			subMonitor.done();
		}

	}

	/**
	 * Creates a new manifest.yml file. If one already exists, the existing one
	 * will not be replaced.
	 *
	 * @return true if new file created with content. False otherwise
	 * @throws Exception
	 *             if error occurred during file creation or serialising
	 *             manifest content
	 */
	public boolean create(IProgressMonitor monitor, CloudApplicationDeploymentProperties properties) throws Exception {

		if (properties == null) {
			return false;
		}
		File file = getManifestFile();
		if (file != null) {
			Log.warn(
					"Manifest.yml file already found at: " + manifestFile.getFullPath() + ". New content will not be written.");
			return false;
		}

		Map<Object, Object> deploymentInfoYaml = toYaml(properties, cloudData);
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		Yaml yaml = new Yaml(new SafeConstructor(), new Representer(options), options);
		String manifestValue = yaml.dump(deploymentInfoYaml);

		if (manifestValue == null) {
			throw ExceptionUtil.coreException("Failed to generate manifesty.yml for: " + properties.getAppName()
					+ " Unknown problem trying to serialise content of manifest into: " + deploymentInfoYaml);
		}

		createFile(project, manifestFile, manifestValue, monitor);
		return true;
	}

	public static Map<Object, Object> toYaml(DeploymentProperties properties, CloudData cloudData) {
		return toYaml(properties, cloudData, false);
	}

	@SuppressWarnings("unchecked")
	public static Map<Object, Object> toYaml(DeploymentProperties properties, CloudData cloudData, boolean legacyHostDomain) {
		Map<Object, Object> deploymentInfoYaml = new LinkedHashMap<>();

		Object applicationsObj = deploymentInfoYaml.get(APPLICATIONS_PROP);
		List<Map<Object, Object>> applicationsList = null;
		if (applicationsObj == null) {
			applicationsList = new ArrayList<>();
			deploymentInfoYaml.put(APPLICATIONS_PROP, applicationsList);
		} else if (applicationsObj instanceof List<?>) {
			applicationsList = (List<Map<Object, Object>>) applicationsObj;
		}

		Map<Object, Object> application = new LinkedHashMap<>();
		applicationsList.add(application);

		application.put(NAME_PROP, properties.getAppName());

		String memory = getMemoryAsString(properties.getMemory());
		if (memory != null) {
			application.put(MEMORY_PROP, memory);
		}

		String diskQuota = getMemoryAsString(properties.getDiskQuota());
		if (diskQuota != null && properties.getDiskQuota() != DeploymentProperties.DEFAULT_MEMORY) {
			application.put(DISK_QUOTA_PROP, diskQuota);
		}

		if (properties.getInstances() != DeploymentProperties.DEFAULT_INSTANCES) {
			application.put(ApplicationManifestHandler.INSTANCES_PROP, properties.getInstances());
		}
		if (properties.getTimeout() != null) {
			application.put(ApplicationManifestHandler.TIMEOUT_PROP, properties.getTimeout());
		}
		String healthCheck = properties.getHealthCheckType();
		if (healthCheck != null) {
			application.put(ApplicationManifestHandler.HEALTH_CHECK_TYPE_PROP, healthCheck);
		}
		String healthCheckHttpEndpoint = properties.getHealthCheckHttpEndpoint();
		if (healthCheckHttpEndpoint != null) {
			application.put(ApplicationManifestHandler.HEALTH_CHECK_HTTP_ENDPOINT_PROP, healthCheckHttpEndpoint);
		}
		if (properties.getCommand() != null) {
			application.put(ApplicationManifestHandler.COMMAND_PROP, properties.getCommand());
		}
		if (properties.getStack() != null) {
			application.put(ApplicationManifestHandler.STACK_PROP, properties.getStack());
		}
		if (properties.getServices() != null && !properties.getServices().isEmpty()) {
			application.put(SERVICES_PROP, properties.getServices());
		}
		if (properties.getEnvironmentVariables() != null && !properties.getEnvironmentVariables().isEmpty()) {
			application.put(ENV_PROP, properties.getEnvironmentVariables());
		}
		if (properties.getBuildpack() != null) {
			application.put(ApplicationManifestHandler.BUILDPACK_PROP, properties.getBuildpack());
		}

		if (legacyHostDomain) {
			Set<String> hosts = new LinkedHashSet<>();
			Set<String> domains = new LinkedHashSet<>();
			List<CFCloudDomain> cloudDomains = cloudData.getDomains();
			extractHostsAndDomains(properties.getUris(), cloudDomains, hosts, domains);
			for (String uri : properties.getUris()) {
				try {
					// Find the first valid URL
					CFRoute route = CFRoute.builder().from(uri, cloudDomains).build();
					if (route.getHost() != null) {
						hosts.add(route.getHost());
					}
					if (route.getDomain() != null) {
						domains.add(route.getDomain());
					}
				} catch (Exception e) {
					// ignore
				}
			}
			if (hosts.isEmpty() && domains.isEmpty()) {
				application.put(NO_ROUTE_PROP, true);
			} else {
				if (hosts.isEmpty()) {
					application.put(NO_HOSTNAME_PROP, true);
				} else if (hosts.size() == 1) {
					String host = hosts.iterator().next();
					if (!properties.getAppName().equals(host)) {
						application.put(SUB_DOMAIN_PROP, host);
					}
				} else {
					application.put(SUB_DOMAINS_PROP, new ArrayList<>(hosts));
				}
				if (domains.size() == 1) {
					application.put(DOMAIN_PROP, domains.iterator().next());
				} else if (domains.size() > 1) {
					application.put(DOMAINS_PROP, new ArrayList<>(domains));
				}
			}
		} else {
			Set<String> uris = properties.getUris();
			if (uris == null || uris.isEmpty()) {
				application.put(NO_ROUTE_PROP, true);
			} else {
				application.put(ApplicationManifestHandler.ROUTES_PROP, uris.stream().map(uri -> {
					Map<Object, Object> route = new LinkedHashMap<>();
					route.put(ApplicationManifestHandler.ROUTE_PROP, uri);
					return route;
				}).collect(Collectors.toList()));
			}
		}

		return deploymentInfoYaml;
	}

	public static void extractHostsAndDomains(Collection<String> uris, List<CFCloudDomain> cloudDomains, Set<String> hostsSet, Set<String> domainsSet) {
		for (String uri : uris) {
			try {
				// Find the first valid URL
				CFRoute route = CFRoute.builder().from(uri, cloudDomains).build();
				if (route.getHost() != null) {
					hostsSet.add(route.getHost());
				}
				if (route.getDomain() != null) {
					domainsSet.add(route.getDomain());
				}
			} catch (Exception e) {
				// ignore
			}
		}
	}

	public void createFile(IProject project, IFile file, String data, IProgressMonitor monitor) throws CoreException {
		file.create(new ByteArrayInputStream(data.getBytes()), true, monitor);
		project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void readEnvVars(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		Map<Object, Object> propertiesMap = new LinkedHashMap<>();
		Map<?, ?> map = getContainingPropertiesMap(allResults, ENV_PROP);
		if (map != null) {
			propertiesMap.putAll(map);
		}
		map = getContainingPropertiesMap(application, ENV_PROP);
		if (map != null) {
			propertiesMap.putAll(map);
		}

		if (propertiesMap.isEmpty()) {
			return;
		}

		Map<String, String> loadedVars = new HashMap<>();

		for (Entry<?, ?> entry : propertiesMap.entrySet()) {
			if ((entry.getKey() instanceof String)) {
				String varName = (String) entry.getKey();
				String varValue = null;
				if (entry.getValue() instanceof String) {
					varValue = (String) entry.getValue();
				} else if (entry.getValue() instanceof Integer) {
					varValue = Integer.toString((Integer) entry.getValue());
				}
				if (varName != null && varValue != null) {
					loadedVars.put(varName, varValue);
				}
			}
		}
		properties.setEnvironmentVariables(loadedVars);
	}

	protected void readServices(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Object yamlElementObj = allResults.get(SERVICES_PROP);
		List<String> cloudServices = new ArrayList<>();

		if (yamlElementObj instanceof List<?>) {
			addTo((List<?>) yamlElementObj, cloudServices);
		}

		yamlElementObj = application.get(SERVICES_PROP);
		if (yamlElementObj instanceof List<?>) {
			addTo((List<?>) yamlElementObj, cloudServices);
		}

		properties.setServices(cloudServices);
	}

	protected void readBuildpacks(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Object yamlElementObj = allResults.get(BUILDPACKS_PROP);
		List<String> buildpacks = new ArrayList<>();

		if (yamlElementObj instanceof List<?>) {
			addTo((List<?>) yamlElementObj, buildpacks);
		}

		yamlElementObj = application.get(BUILDPACKS_PROP);
		if (yamlElementObj instanceof List<?>) {
			addTo((List<?>) yamlElementObj, buildpacks);
		}

		properties.setBuildpacks(buildpacks);
	}

	protected void addTo(List<?> from, List<String> to) {

		for (Object obj : from) {
			if (obj instanceof String && !to.contains(obj)) {
				String val = (String) obj;
				to.add(val);
			}
		}
	}

	protected void readInstances(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Integer instances = getValue(application, INSTANCES_PROP, Integer.class);
		if (instances == null) {
			instances = getValue(allResults, INSTANCES_PROP, Integer.class);
		}
		if (instances != null) {
			properties.setInstances(instances);
		}
	}

	protected void readTimeout(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		Integer timeout = getValue(application, TIMEOUT_PROP, Integer.class);
		if (timeout == null) {
			timeout = getValue(allResults, TIMEOUT_PROP, Integer.class);
		}
		if (timeout != null) {
			properties.setTimeout(timeout);
		}
	}

	private void readHealthCheckType(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		String hc = getValue(application, HEALTH_CHECK_TYPE_PROP, String.class);
		if (hc == null) {
			hc = getValue(allResults, HEALTH_CHECK_TYPE_PROP, String.class);
		}
		if (hc != null) {
			properties.setHealthCheckType(hc);
		}
	}

	private void readHealthCheckHttpEndpoint(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		String hche = getValue(application, HEALTH_CHECK_HTTP_ENDPOINT_PROP, String.class);
		if (hche == null) {
			hche = getValue(allResults, HEALTH_CHECK_HTTP_ENDPOINT_PROP, String.class);
		}
		if (hche != null) {
			properties.setHealthCheckHttpEndpoint(hche);
		}
	}

	protected void readBuildpack(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		String buildpack = getValue(application, BUILDPACK_PROP, String.class);
		if (buildpack == null) {
			buildpack = getValue(allResults, BUILDPACK_PROP, String.class);
		}
		if (buildpack != null) {
			properties.setBuildpack(buildpack);
		}
	}

	protected void readCommand(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		String command = getValue(application, COMMAND_PROP, String.class);
		if (command == null) {
			command = getValue(allResults, COMMAND_PROP, String.class);
		}
		if (command != null) {
			properties.setCommand(command);
		}
	}

	protected void readStack(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {

		String stack = getValue(application, STACK_PROP, String.class);
		if (stack == null) {
			stack = getValue(allResults, STACK_PROP, String.class);
		}
		if (stack != null && isStackValid(stack, cloudData.getStacks())) {
			properties.setStack(stack);
		}
	}

	private boolean noRoute(Map<?, ?> application, Map<Object, Object> allResults) {
		Boolean noRoute = getValue(application, NO_ROUTE_PROP, Boolean.class);
		if (noRoute == null) {
			noRoute = getValue(allResults, NO_ROUTE_PROP, Boolean.class);
		}
		return Boolean.TRUE.equals(noRoute);
	}

	/**
	 *
	 * @param application
	 * @param allResults
	 * @param properties
	 * @param randomRoute true if random host should be generated
	 * @return non-null list of URIs parsed from domains and hosts. May be empty
	 */
	@SuppressWarnings("unchecked")
	private List<String> fromDomainsAndHosts(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties, boolean randomRoute) {

		HashSet<String> hostsSet = new LinkedHashSet<>();
		HashSet<String> domainsSet = new LinkedHashSet<>();

		/*
		 * Gather domains from app node and root node from 'domain' and 'domains' attributes
		 */
		String domain = getValue(application, DOMAIN_PROP, String.class);
		if (domain == null) {
			domain = getValue(allResults, DOMAIN_PROP, String.class);
		}
		if (domain != null) {
			domainsSet.add(domain);
		}
		List<String> domainList = (List<String>) getValue(allResults, DOMAINS_PROP, List.class);
		if (domainList != null) {
			domainsSet.addAll(domainList);
		}
		domainList = (List<String>) getValue(application, DOMAINS_PROP, List.class);
		if (domainList != null) {
			domainsSet.addAll(domainList);
		}

		/*
		 * Gather domains from app node and root node from 'host' and 'hosts'
		 * attributes. Account for ${random} in host's name
		 */
		String host = getValue(application, SUB_DOMAIN_PROP, String.class);
		if (host == null) {
			host = getValue(allResults, SUB_DOMAIN_PROP, String.class);
		}
		if (host != null) {
			hostsSet.add(host);
		}
		List<String> hostList = (List<String>) getValue(allResults, SUB_DOMAINS_PROP, List.class);
		if (hostList != null) {
			hostsSet.addAll(hostList);
		}
		hostList = (List<String>) getValue(application, SUB_DOMAINS_PROP, List.class);
		if (hostList != null) {
			hostsSet.addAll(hostList);
		}

		/*
		 * If no host names found check for "random-route: true" and
		 * "no-hostname: true" otherwise take app name as the host name
		 */
		if (hostsSet.isEmpty()) {
			if (randomRoute) {
				//hostsSet.add(extractHost("${random}", 10));
				if (domainsSet.isEmpty()) {
					domainsSet.add(cloudData.getDefaultDomain());
				}
			} else {
				Boolean noHostName = getValue(application, NO_HOSTNAME_PROP, Boolean.class);
				if (noHostName == null) {
					noHostName = getValue(allResults, NO_HOSTNAME_PROP, Boolean.class);
				}
				if (!Boolean.TRUE.equals(noHostName)) {
					/*
					 * Assumes name is set before URIs are processed
					 */
					hostsSet.add(properties.getAppName());
				}
			}
		}

		/*
		 * Set a domain if they are still empty
		 */
		if (domainsSet.isEmpty()) {
			domainsSet.add(cloudData.getDefaultDomain());
		}

		/*
		 * Compose URIs for application based on hosts and domains
		 */
		List<String> uris = new ArrayList<>(hostsSet.isEmpty() ? 1 : hostsSet.size() * domainsSet.size());
		for (String d : domainsSet) {
			if (hostsSet.isEmpty()) {
				uris.add(CFRoute.builder().domain(d).build().getRoute());
			} else {
				for (String h : hostsSet) {
					uris.add(CFRoute.builder().host(h).domain(d).build().getRoute());
				}
			}
		}

		return uris;
	}

	protected void readApplicationURL(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		Boolean randomRoute = getValue(application, RANDOM_ROUTE_PROP, Boolean.class);
		if (randomRoute == null) {
			randomRoute = getValue(allResults, RANDOM_ROUTE_PROP, Boolean.class);
		}
        boolean useRandomRoute = Boolean.TRUE.equals(randomRoute);
        properties.setRandomRoute(useRandomRoute);

		/*
		 * Check for "no-route: true". If set then uris list should be empty
		 */
		if (!noRoute(application, allResults)) {
			// Manifest documentation states:
			// "The routes attribute cannot be used in conjunction with the
			// following
			// attributes: host, hosts, domain, domains, and no-hostname.
			// An error will result."
			// If only routes are available, then only parse routes. Do NOT
			// also create a default URI from domain and app name if routes are available.
			// This appears to be consistent with cf CLI behaviour as well.
			// Otherwise fall back to parsing from domains and hosts
			List<String> uris = null;
			if (hasRoutesProperty(application, allResults)) {
				uris = fromRoutesProperty(application, allResults, properties);
			} else {
				uris = fromDomainsAndHosts(application, allResults, properties, useRandomRoute);
			}
			properties.setUris(uris);
		}
	}

	private List<String> fromRoutesProperty(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) {
		Set<String> uris = new LinkedHashSet<>();
		List<CFCloudDomain> domains = cloudData.getDomains();
		List<?> routes = getValue(application, ROUTES_PROP, List.class);
		List<?> rootRoutes = getValue(allResults, ROUTES_PROP, List.class);
		if (routes != null || rootRoutes != null) {
			routes = routes == null ? Collections.emptyList() : routes;
			rootRoutes = rootRoutes == null ? Collections.emptyList() : rootRoutes;

			return ImmutableList.copyOf(Stream.concat(routes.stream(), rootRoutes.stream())
				.filter(o -> o instanceof Map<?,?>)
				.map(o -> (Map<?,?>) o)
				.map(routeMap -> routeMap.get(ROUTE_PROP))
				.filter(Objects::nonNull)
				.filter(route -> route instanceof String)
				.map(route -> {
					String url = (String) route;
					CFRoute rt = CFRoute.builder().from(url, domains).build();
					return rt.getRoute();
				})
				.collect(Collectors.toSet()));
		}
		return ImmutableList.copyOf(uris);
	}

	protected boolean hasRoutesProperty(Map<?, ?> application, Map<Object, Object> allResults) {
		return hasValue(application, ROUTES_PROP) || hasValue(allResults, ROUTES_PROP);
	}

	public static boolean isStackValid(String stack, List<CFStack> stacks) {
		for (CFStack cloudStack : stacks) {
			if (cloudStack.getName().equals(stack)) {
				return true;
			}
		}
		return false;
	}

	private String extractHost(String subdomain, int length) {
		// Check for random word
		int varIndex = subdomain.indexOf(RANDOM_VAR);
		while (varIndex >= 0)  {
			String randomWord = RandomStringUtils.randomAlphabetic(length);
			subdomain = subdomain.replace(subdomain.substring(varIndex, RANDOM_VAR.length()), randomWord);
			varIndex = subdomain.indexOf(RANDOM_VAR);
		}
		return subdomain;
	}

	protected void readMemory(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) throws Exception {
		int memoryValue = readMemoryValue(application, allResults, MEMORY_PROP);
		if (memoryValue >= 0) {
			properties.setMemory(memoryValue);
		}
	}

	protected void readDiskQuota(Map<?, ?> application, Map<Object, Object> allResults,
			CloudApplicationDeploymentProperties properties) throws Exception {
		int memoryValue = readMemoryValue(application, allResults, DISK_QUOTA_PROP);
		if (memoryValue >= 0) {
			properties.setDiskQuota(memoryValue);
		}
	}

	protected int readMemoryValue(Map<?, ?> application, Map<Object, Object> allResults,
			String propertyKey) throws Exception {
		// Check if value in integer form
		Integer memoryVal = getValue(application, propertyKey, Integer.class);
		if (memoryVal != null) {
			return memoryVal.intValue();
		}

		// If not in Integer form, try String as the memory may end in with a 'G' or 'M'
		String memoryStringVal = getValue(application, propertyKey, String.class);
		if (memoryStringVal == null) {
			// Check if there is memory property set for all apps if nothing set for the app specifically
			memoryVal = getValue(allResults, propertyKey, Integer.class);
			if (memoryVal == null) {
				// Not in integer form
				memoryStringVal = getValue(allResults, propertyKey, String.class);
			} else {
				// Integer form? Return the value right away
				return memoryVal.intValue();
			}
		}

		// Should only get here if memory value not in integer form
		if (memoryStringVal != null && memoryStringVal.length() > 0) {
			// Parse non-integer memory value
			return convertMemory(memoryStringVal);
		}

		// No memory property specified? Assume the default
		return DeploymentProperties.DEFAULT_MEMORY;
	}

	public static int convertMemory(String memoryStringVal) throws CoreException {
		String memoryIndicator[] = { "m", "g", "mb", "gb" };
		int gIndex = -1;
		boolean gb = false;

		for (String indicator : memoryIndicator) {
			int beginIndex = memoryStringVal.length() - indicator.length();
			if (beginIndex >= 0) {
				if (indicator.equalsIgnoreCase(memoryStringVal.substring(beginIndex))) {
					gIndex = beginIndex;
					gb = indicator.charAt(0) == 'g';
					break;
				}
			}
		}

		// There has to be a number before the 'G' or 'M', if 'G' or 'M'
		// is used, or its not a valid
		// memory
		if (gIndex > 0) {
			memoryStringVal = memoryStringVal.substring(0, gIndex);
		} else if (gIndex == 0) {
			throw ExceptionUtil.coreException("Failed to read memory value. Invalid memory: " + memoryStringVal);
		}

		try {
			return Integer.valueOf(memoryStringVal) * (gb ? 1024 : 1);
		} catch (NumberFormatException e) {
			throw ExceptionUtil.coreException("Failed to parse memory due to: " + e.getMessage());
		}
	}

	/**
	 *
	 * @return map of parsed manifest file, if the file exists. If the file does
	 *         not exist, return null.
	 * @throws CoreException
	 *             if manifest file exists, but error occurred that prevents a
	 *             map to be generated.
	 */
	@SuppressWarnings("unchecked")
	protected Map<Object, Object> parseManifestFromFile() throws Exception {

		InputStream inputStream = getInputStream();

		if (inputStream != null) {
			Yaml yaml = new Yaml(new SafeConstructor());

			try {
				Object results = yaml.load(inputStream);

				if (results instanceof Map) {
					return (Map<Object, Object>) results;
				} else {
					String source = manifestFile == null ? "entered manifest" : "file " + manifestFile.getFullPath();
					throw ExceptionUtil.coreException("Expected a map of values for "
							+ source + ". Unable to load manifest content.  Actual results: " + results);
				}

			} finally {
				try {
					inputStream.close();
				} catch (IOException e) {
					// Ignore
				}
			}

		}
		return null;
	}

	static protected String getMemoryAsString(int memory) {
		if (memory < 1) {
			return null;
		}
		return memory + "M";
	}

}
