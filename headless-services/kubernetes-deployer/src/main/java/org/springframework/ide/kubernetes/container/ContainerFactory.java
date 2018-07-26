/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.kubernetes.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition;

import io.fabric8.kubernetes.api.model.Container;
import io.fabric8.kubernetes.api.model.ContainerBuilder;
import io.fabric8.kubernetes.api.model.EnvVar;

public class ContainerFactory {

	private static Log logger = LogFactory.getLog(ContainerFactory.class);

	public Container create(DeploymentDefinition request) {

		String image = request.getDockerImage().getUri().getSchemeSpecificPart();

		logger.info("Using Docker image: " + image);

		Map<String, String> envVarsMap = new HashMap<>();
		for (String envVar : request.getEnvironmentVariables()) {
			String[] strings = envVar.split("=", 2);
			envVarsMap.put(strings[0], strings[1]);
		}

		List<EnvVar> envVars = new ArrayList<>();
		for (Map.Entry<String, String> e : envVarsMap.entrySet()) {
			envVars.add(new EnvVar(e.getKey(), e.getValue(), null));
		}

		ContainerBuilder container = new ContainerBuilder();
		container.withName(request.getAppName()).withImage(image).withEnv(envVars);

		Integer port = request.getContainerPort();

		if (port != null) {
			if (request.isHostNetwork()) {
				container.addNewPort().withContainerPort(port).withHostPort(port).endPort();
			} else {
				container.addNewPort().withContainerPort(port).endPort();
			}
		}

		return container.build();
	}

}
