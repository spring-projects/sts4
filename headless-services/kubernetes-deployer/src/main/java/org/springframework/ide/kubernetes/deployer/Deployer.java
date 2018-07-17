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

import org.springframework.ide.kubernetes.AppDeployer;
import org.springframework.ide.kubernetes.ClientFactory;
import org.springframework.ide.kubernetes.ContainerFactory;
import org.springframework.ide.kubernetes.DeploymentDefinition;
import org.springframework.ide.kubernetes.DockerImage;

import io.fabric8.kubernetes.api.model.ServicePort;

public class Deployer {

	private ClientFactory clientFactory = new ClientFactory();


	public void deploy(String appName, String image, ServicePort servicePort, int containerPort, int replicas, boolean useNodePort) {

		DeploymentDefinition definition = new DeploymentDefinition();
		definition.setContainerPort(containerPort);
		definition.setServicePort(servicePort);
		definition.setDockerImage(getDockerImage(image));
		definition.setAppName(appName);
		definition.setNodePort(useNodePort);
		definition.setReplicaCount(replicas);
		definition.setImagePullPolicy("Always");

		ContainerFactory containerFactory = new ContainerFactory();
		AppDeployer deployer = new AppDeployer(clientFactory, containerFactory);
		deployer.deploy(definition);

	}

	private DockerImage getDockerImage(String image) {
		return new DockerImage(image);
	}
}
