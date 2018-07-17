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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.ServicePort;

public class DeployerCommandLineRunner implements CommandLineRunner {

	private Logger logger = LoggerFactory.getLogger(DeployerCommandLineRunner.class);
	
	final private static int DEFAULT_CONTAINER_PORT = 8080;

	@Autowired
	private Deployer deployer;
	
	@Autowired
	private DeployerArgsParser argsParser;

	@Override
	public void run(String... args) throws Exception {
		boolean useNodePort = argsParser.useNodePort(args);
		String appName = argsParser.getAppName(args);
		String image = argsParser.getDockerImage(args);
		
		if (!validate(appName, image)) {
			logger.error(argsParser.getUsageMessage());
			return;
		}

		logger.info("Deploying app " + appName + " to PKS");
		logger.info("Application image: " + image);
		if (useNodePort) {
			logger.info("Using 'NodePort' for PKS deployment");
		}
		ServicePort servicePort = argsParser.getSpringBootServicePort(useNodePort);
		int replicas = argsParser.getReplicas(args);
		
		int containerPort = DEFAULT_CONTAINER_PORT;
		deployer.deploy(appName, image, servicePort, containerPort, replicas, useNodePort);
	}

	private boolean validate(String appName, String image) {
		boolean valid = true;
		if (!StringUtils.hasText(appName)) {
			logger.error("Missing application name");
			valid = false;
		}
		if (!StringUtils.hasText(image)) {
			logger.error("Missing docker image");
			valid = false;
		}
		return valid;
		
	}
}
