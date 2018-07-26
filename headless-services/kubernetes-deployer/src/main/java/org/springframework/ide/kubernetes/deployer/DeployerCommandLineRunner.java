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
import org.springframework.boot.CommandLineRunner;
import org.springframework.ide.kubernetes.AppDeployer;
import org.springframework.ide.kubernetes.DeploymentDefinition;
import org.springframework.ide.kubernetes.DeploymentDefinition.DeploymentCommand;
import org.springframework.util.StringUtils;

public class DeployerCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(DeployerCommandLineRunner.class);

	@Autowired
	private DeployerArgsParser argsParser;

	@Autowired
	private AppDeployer deployer;

	@Override
	public void run(String... args) throws Exception {

		DeploymentDefinition definition = argsParser.toDefinition(args);

		if (!validate(definition)) {
			return;
		}

		logInfo(definition);

		run(definition);
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

	private boolean validate(DeploymentDefinition definition) {
		boolean valid = true;
		if (!StringUtils.hasText(definition.getAppName())) {
			logger.error("Missing application name");
			valid = false;
		}

		if (definition.getDeploymentCommand() == null) {
			logger.error("Missing deployment command. Valid values: " + Arrays.toString(DeploymentCommand.values()));
			valid = false;
		}
		return valid;

	}

	public void run(DeploymentDefinition definition) {

		switch (definition.getDeploymentCommand()) {
		case deploy:
			deployer.deploy(definition);
			break;
		case undeploy:
			deployer.undeploy(definition);
			break;
		}

	}
}
