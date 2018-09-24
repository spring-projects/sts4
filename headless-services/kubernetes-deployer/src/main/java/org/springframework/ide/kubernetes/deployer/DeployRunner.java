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
package org.springframework.ide.kubernetes.deployer;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.kubernetes.container.DockerHandler;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition.DeploymentCommand;
import org.springframework.util.StringUtils;

public class DeployRunner {

	private Logger logger = LoggerFactory.getLogger(DeployRunner.class);
	private final AppDeployer deployer;
	private final DockerHandler dockerHandler;
	private final String RESULT_LOG_PREFIX = "STS KUBERNETES";

	@Autowired
	public DeployRunner(AppDeployer deployer, DockerHandler dockerHandler) {
		this.deployer = deployer;
		this.dockerHandler = dockerHandler;
	}

	public void run(DeploymentDefinition definition) throws Exception {

		validate(definition);

		logger.info(definition.getDeploymentCommand() + " app " + definition.getAppName());

		switch (definition.getDeploymentCommand()) {
		case update:
			dockerPush(definition);
			break;
		case deploy:
			dockerPush(definition);
			List<String> uris = deployer.deploy(definition);
			logUris(uris);
			break;
		case undeploy:
			deployer.undeploy(definition);
			break;
		case services:
			List<String> services = deployer.getExistingServices();
			logServices(services);
			break;
		}
	}

	private void dockerPush(DeploymentDefinition definition) throws Exception {
		if (definition.getJarPath() != null) {
			dockerHandler.createImageAndPush(definition.getJarPath(), definition.getDockerImage());
		}
	}

	private void logUris(List<String> uris) {
		if (uris != null && !uris.isEmpty()) {
			logger.info(RESULT_LOG_PREFIX + "- URI: " + "http://" + uris.get(0) + '\n');
		}
	}
	
	private void logServices(List<String> services) {
		if (services != null && !services.isEmpty()) {
			for (String service : services) {
				logger.info(RESULT_LOG_PREFIX + "- Service: " + "http://" + service + '\n');
			}
		}
	}

	private void validate(DeploymentDefinition definition) throws Exception {
		if (definition == null) {
			throw new IllegalArgumentException("No deployment definition provided");
		}
		if (!StringUtils.hasText(definition.getAppName())) {
			throw new IllegalArgumentException("Missing application name");
		}

		if (definition.getDeploymentCommand() == null) {
			throw new IllegalArgumentException(
					"Missing deployment command. Valid values: " + Arrays.toString(DeploymentCommand.values()));
		}
	}
}
