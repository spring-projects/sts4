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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition.DeploymentDefinitionBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DeployController {

	private final KubernetesSpringService kubernetesService;

	@Autowired
	public DeployController(KubernetesSpringService kubernetesService) {
		this.kubernetesService = kubernetesService;
	}

	@RequestMapping("/deploy")
	public DeploymentResult deploy(@RequestParam(value = "name") String name,
			@RequestParam(value = "image") String image, //
			@RequestParam(value = "jarPath") String jarPath, //
			@RequestParam(value = "useNodePort") String useNodePort, //
			@RequestParam(value = "replicaCount") String replicaCount) throws Exception {

		DeploymentDefinitionBuilder builder = DeploymentDefinition.builder();

		builder.appName(name) //
				.useNodePort(useNodePort) //
				.dockerImage(image) //
				.replicaCount(replicaCount) //
				.jarPath(jarPath) //
				.imagePullPolicy("Always") //
				.restartPolicy("Never") //
				.hostNetwork(false) //
				.containerPort(DeploymentDefinition.DEFAULT_CONTAINER_PORT);

		DeploymentDefinition definition = builder.build();

		return kubernetesService.deploy(definition);
	}
	
	@RequestMapping("/undeploy")
	public DeploymentResult undeploy(@RequestParam(value = "name") String name) throws Exception {

		DeploymentDefinitionBuilder builder = DeploymentDefinition.builder();

		builder.appName(name);

		DeploymentDefinition definition = builder.build();

		return kubernetesService.deploy(definition);
	}
	
	@RequestMapping("/services")
	public ServicesResult services() throws Exception {
		return kubernetesService.getServices();
	}

}
