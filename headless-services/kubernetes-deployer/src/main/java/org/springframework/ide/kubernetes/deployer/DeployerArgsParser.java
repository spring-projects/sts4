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
import org.springframework.ide.kubernetes.container.DockerImage;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition.DeploymentCommand;
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;

public class DeployerArgsParser {

	private Logger logger = LoggerFactory.getLogger(DeployerArgsParser.class);
	final private static int DEFAULT_CONTAINER_PORT = 8080;

	public String getDockerImage(String... args) {
		for (String arg : args) {
			if (arg.startsWith("image:")) {
				String[] vals = arg.split("image:");
				return vals.length == 2 ? vals[1] : null;
			}
		}
		return null;
	}

	public String getJarPath(String... args) {
		for (String arg : args) {
			if (arg.startsWith("jarPath:")) {
				String[] vals = arg.split("jarPath:");
				return vals.length == 2 ? vals[1] : null;
			}
		}
		return null;
	}

	public DeploymentCommand getCommand(String... args) {
		DeploymentCommand[] values = DeploymentCommand.values();
		for (DeploymentCommand deploymentCommand : values) {
			for (String arg : args) {
				if (arg.equals(deploymentCommand.toString())) {
					return deploymentCommand;
				}
			}
		}

		return null;
	}

	public int getReplicas(String... args) {
		int replica = 1;
		for (String arg : args) {
			if (arg.startsWith("replicas:")) {
				String[] vals = arg.split("replicas:");
				if (vals.length == 2 && vals[1] != null) {
					try {
						replica = Integer.parseInt(vals[1]);
					} catch (NumberFormatException e) {
						logger.error("", e);
					}
				}
			}
		}
		return replica;
	}

	public String getAppName(String... args) {
		for (String arg : args) {
			if (arg.startsWith("name:")) {
				String[] vals = arg.split("name:");
				return vals.length == 2 ? vals[1] : null;
			}
		}
		return null;
	}

	public boolean useNodePort(String... args) {
		for (String arg : args) {
			if (arg.startsWith("use-node-port:") && arg.contains("true")) {
				return true;
			}
		}
		return false;
	}

	public DeploymentDefinition toDefinition(String... args) {
		Integer containerPort = DEFAULT_CONTAINER_PORT;
		String imagePullPolicy = "Always";
		DeployerArgsParser argsParser = new DeployerArgsParser();

		boolean useNodePort = argsParser.useNodePort(args);

		ServicePort servicePort = new ServicePort();
		if (useNodePort) {
			int targetPort = 8080;
			int port = 80;

			// Configure the spring boot "service" to use "port" and "targetPort" for use in
			// node port setting
			servicePort.setPort(port);
			servicePort.setTargetPort(new IntOrString(targetPort));
		}

		String appName = argsParser.getAppName(args);
		String image = argsParser.getDockerImage(args);
		String jarPath = argsParser.getJarPath(args);
		DeploymentCommand command = argsParser.getCommand(args);
		int replicas = argsParser.getReplicas(args);

		DeploymentDefinition definition = new DeploymentDefinition(appName, command);
		definition.setContainerPort(containerPort);
		definition.setJarPath(jarPath);
		definition.setServicePort(servicePort);
		
		if (StringUtils.hasText(image)) {
			definition.setDockerImage(new DockerImage(image));
		}
		
		definition.setNodePort(useNodePort);
		definition.setReplicaCount(replicas);
		definition.setImagePullPolicy(imagePullPolicy);
		return definition;
	}

}
