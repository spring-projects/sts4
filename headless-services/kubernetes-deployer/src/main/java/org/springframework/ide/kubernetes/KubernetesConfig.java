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
package org.springframework.ide.kubernetes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.kubernetes.container.ContainerFactory;
import org.springframework.ide.kubernetes.container.DockerClientFactory;
import org.springframework.ide.kubernetes.container.DockerHandler;
import org.springframework.ide.kubernetes.deployer.AppDeployer;
import org.springframework.ide.kubernetes.deployer.DeployRunner;
import org.springframework.ide.kubernetes.deployer.DeployerArgsParser;
import org.springframework.ide.kubernetes.deployer.KubernetesAppDeployer;
import org.springframework.ide.kubernetes.deployer.KubernetesClientFactory;

@Configuration
public class KubernetesConfig {

	@Bean
	public DeployerArgsParser getDepoyerArgsParser() {
		return new DeployerArgsParser();
	}

	@Bean
	public KubernetesClientFactory getClientFactory() {
		return new KubernetesClientFactory();
	}

	@Bean
	public ContainerFactory getContainerFactory() {
		return new ContainerFactory();
	}
	
	@Bean
	public DockerClientFactory getDockerClientFactory() {
		return new DockerClientFactory();
	}

	@Bean
	public AppDeployer getAppDeployer(ContainerFactory containerFactory, KubernetesClientFactory clientFactory) {
		return new KubernetesAppDeployer(containerFactory, clientFactory);
	}
	
	@Bean
	public DockerHandler getDockerHandler(DockerClientFactory clientFactory) {
		return new DockerHandler(clientFactory);
	}
	
	@Bean
	public DeployRunner getDeployerRunner(AppDeployer appDeployer, DockerHandler dockerHandler) {
		return new DeployRunner(appDeployer, dockerHandler);
	}

}
