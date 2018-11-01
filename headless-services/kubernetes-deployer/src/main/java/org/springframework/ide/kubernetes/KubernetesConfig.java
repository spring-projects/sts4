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
import org.springframework.ide.kubernetes.container.DockerClientFactory;
import org.springframework.ide.kubernetes.container.DockerImageHandler;
import org.springframework.ide.kubernetes.deployer.KubernetesClientFactory;
import org.springframework.ide.kubernetes.deployer.KubernetesSpringService;

@Configuration
public class KubernetesConfig {

	@Bean
	public KubernetesClientFactory getKubernetesClientFactory() {
		return new KubernetesClientFactory();
	}

	@Bean
	public DockerClientFactory getDockerClientFactory() {
		return new DockerClientFactory();
	}

	@Bean
	public DockerImageHandler getDockerImageHandler(DockerClientFactory dockerClientFactory) {
		return new DockerImageHandler(dockerClientFactory);
	}

	@Bean
	public KubernetesSpringService getDeployerService(KubernetesClientFactory clientFactory,
			DockerImageHandler dockerHandler) {
		return new KubernetesSpringService(clientFactory, dockerHandler);
	}

}
