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

	String getAppName();

	int getMemory();

	int getDiskQuota();

	Integer getTimeout();

	String getHealthCheckType();

	String getBuildpack();

	String getCommand();

	String getStack();

	Map<String, String> getEnvironmentVariables();

	int getInstances();

	List<String> getServices();

	Set<String> getUris();

}
