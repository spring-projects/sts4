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
import org.springframework.util.StringUtils;

import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;

public class DeploymentDefinition {

	final public static int DEFAULT_CONTAINER_PORT = 8080;

	private boolean isHostNetwork;
	private Integer containerPort;

	private ServicePort servicePort;
	private boolean useNodePort;
	private int replicaCount = 1;

	private String restartPolicy;
	private String imagePullPolicy;

	private DockerImage image;
	private String jarPath;

	private String appName;

	private boolean hostNetwork;

	public DeploymentDefinition() {

	}

	public DockerImage getDockerImage() {
		return image;
	}

	public Integer getContainerPort() {
		return containerPort;
	}

	public boolean isHostNetwork() {
		return isHostNetwork;
	}

	public ServicePort getServicePort() {
		return servicePort;
	}

	public boolean useNodePort() {
		return useNodePort;
	}

	public int getReplicaCount() {
		return replicaCount;
	}

	public String getImagePullPolicy() {
		return imagePullPolicy;
	}

	public String getRestartPolicy() {
		return restartPolicy;
	}

	public String getAppName() {
		return appName;
	}

	public String getJarPath() {
		return jarPath;
	}

	public boolean getHostNetwork() {
		return hostNetwork;
	}

	private void setName(String appName) {
		this.appName = appName;
	}

	public static DeploymentDefinitionBuilder builder() {
		return new DeploymentDefinitionBuilder();
	}

	public static class DeploymentDefinitionBuilder {

		private Logger logger = LoggerFactory.getLogger(DeploymentDefinitionBuilder.class);

		private DockerImage image;
		private String jarPath;
		private int replicaCount;
		private String appName;
		private ServicePort servicePort;
		private String restartPolicy;
		private String imagePullPolicy;
		private boolean useNodePort;
		private int containerPort;

		private boolean hostNetwork;

		public DeploymentDefinitionBuilder dockerImage(String image) {
			if (StringUtils.hasText(image)) {
				this.image = new DockerImage(image);
			}
			return this;
		}

		public DeploymentDefinitionBuilder jarPath(String jarPath) {
			this.jarPath = jarPath;
			return this;
		}

		public DeploymentDefinitionBuilder replicaCount(String replicaVal) {
			this.replicaCount = 1;
			if (replicaVal != null) {
				try {
					this.replicaCount = Integer.parseInt(replicaVal);
				} catch (NumberFormatException e) {
					logger.error("", e);
				}
			}

			return this;
		}

		public DeploymentDefinitionBuilder appName(String appName) {
			this.appName = appName;
			return this;
		}

		public DeploymentDefinitionBuilder useNodePort(String useNodePortVal) {
			if (useNodePortVal != null) {
				this.servicePort = new ServicePort();
				boolean useNodePort = Boolean.parseBoolean(useNodePortVal);
				if (useNodePort) {
					this.useNodePort = useNodePort;
					int targetPort = 8080;
					int port = 80;

					// Configure the spring boot "service" to use "port" and "targetPort" for use in
					// node port setting
					servicePort.setPort(port);
					servicePort.setTargetPort(new IntOrString(targetPort));
				}
			}
			return this;
		}

		public DeploymentDefinitionBuilder restartPolicy(String restartPolicy) {
			this.restartPolicy = restartPolicy;
			return this;
		}

		public DeploymentDefinitionBuilder imagePullPolicy(String imagePullPolicy) {
			this.imagePullPolicy = imagePullPolicy;
			return this;
		}

		public DeploymentDefinitionBuilder containerPort(int containerPort) {
			this.containerPort = containerPort;
			return this;
		}

		public DeploymentDefinitionBuilder hostNetwork(boolean hostNetwork) {
			this.hostNetwork = hostNetwork;
			return this;
		}

		public DeploymentDefinition build() {
			DeploymentDefinition definition = new DeploymentDefinition();
			definition.setName(appName);
			definition.image = image;
			definition.jarPath = jarPath;
			definition.replicaCount = replicaCount;
			definition.servicePort = servicePort;
			definition.containerPort = containerPort;
			definition.imagePullPolicy = imagePullPolicy;
			definition.restartPolicy = restartPolicy;
			definition.useNodePort = useNodePort;
			definition.hostNetwork = hostNetwork;

			return definition;
		}

	}

}
