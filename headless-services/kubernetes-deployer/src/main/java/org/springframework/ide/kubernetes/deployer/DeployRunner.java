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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition.DeploymentCommand;
import org.springframework.util.StringUtils;

public class DeployRunner {

	private Logger logger = LoggerFactory.getLogger(DeployRunner.class);
	private final AppDeployer deployer;

	@Autowired
	public DeployRunner(AppDeployer deployer) {
		this.deployer = deployer;
	}

	public void run(DeploymentDefinition definition) throws Exception {

		validate(definition);

		logInfo(definition);

		switch (definition.getDeploymentCommand()) {
		case deploy:
			deployer.deploy(definition);
			break;
		case undeploy:
			deployer.undeploy(definition);
			break;
		}
	}

	private void logInfo(DeploymentDefinition definition) {
		logger.info(definition.getDeploymentCommand() + " app " + definition.getAppName());
		logger.info("Application image: " + definition.getDockerImage().getUri());
		if (definition.createNodePort()) {
			logger.info("Using 'NodePort' for deployment");
			if (definition.getServicePort().getPort() != null) {
				logger.info("Setting service port: " + definition.getServicePort().getPort());
			}

			if (definition.getServicePort().getTargetPort() != null) {
				logger.info("Setting service target port: " + definition.getServicePort().getTargetPort());
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
