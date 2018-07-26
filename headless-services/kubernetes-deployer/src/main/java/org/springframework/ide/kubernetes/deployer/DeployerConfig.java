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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.kubernetes.AppDeployer;
import org.springframework.ide.kubernetes.KubernetesClientWrapper;
import org.springframework.ide.kubernetes.ContainerFactory;
import org.springframework.ide.kubernetes.KubernetesAppDeployer;

@Configuration
public class DeployerConfig {

	@Bean
	public DeployerArgsParser getDepoyerArgsParser() {
		return new DeployerArgsParser();
	}

	@Bean
	public KubernetesClientWrapper getClientFactory() {
		return new KubernetesClientWrapper();
	}

	@Bean
	public ContainerFactory getContainerFactory() {
		return new ContainerFactory();
	}

	@Bean
	public AppDeployer getAppDeployer() {
		return new KubernetesAppDeployer();
	}

}
