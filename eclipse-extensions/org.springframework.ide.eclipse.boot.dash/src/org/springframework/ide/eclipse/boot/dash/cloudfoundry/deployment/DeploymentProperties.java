/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Interface for Cloud Foundry application deployment properties
 *
 * @author Alex Boyko
 *
 */
public interface DeploymentProperties {

	int DEFAULT_MEMORY = 1024;
	int DEFAULT_INSTANCES = 1;
	String DEFAULT_HEALTH_CHECK_TYPE = "port";
	String DEFAULT_HEALTH_CHECK_HTTP_ENDPOINT = "/";

	String getAppName();

	int getMemory();

	int getDiskQuota();

	Integer getTimeout();

	String getHealthCheckType();

	String getHealthCheckHttpEndpoint();

	String getBuildpack();

	List<String> getBuildpacks();

	String getCommand();

	String getStack();

	Map<String, String> getEnvironmentVariables();

	int getInstances();

	List<String> getServices();

	Set<String> getUris();

	/**
	 * If the origin of these properties is parsing yml content (from file or
	 * embedded editor buffer, then this method retrieves the raw text of the
	 * yml content). If the properties where obtained some other way (e.g. by reading
	 * app state from CF itself, it returns null).
	 */
	default String getYamlContent() { return null; }

}
