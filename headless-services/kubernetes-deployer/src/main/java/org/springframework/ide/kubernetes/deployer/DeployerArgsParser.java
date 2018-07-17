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

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;

public class DeployerArgsParser {

	private static final String USAGE_MESSAGE = "Invalid arguments.  Specify the following: 'name:[appname] image:[dockerimage] replicas:[0-9] use-node-port:[true/false]";
	private Logger logger = LoggerFactory.getLogger(DeployerArgsParser.class);

	public String getDockerImage(String... args) {
		for (String arg : args) {
			if (arg.startsWith("image:")) {
				String[] vals = arg.split("image:");
				return vals.length == 2 ? vals[1] : null;
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

	public ServicePort getSpringBootServicePort(boolean useNodePort) {
		ServicePort servicePort = new ServicePort();
		if (useNodePort) {
			int targetPort = 8080;
			int port = 80;

			// Configure the spring boot "service" to use "port" and "targetPort" for use in
			// node port setting
			servicePort.setPort(port);
			logger.info("Setting service port: " + port);
			servicePort.setTargetPort(new IntOrString(targetPort));
			logger.info("Setting service target port: " + targetPort);
		}

		return servicePort;
	}

	public String getUsageMessage() {
		return USAGE_MESSAGE;
	}

}
