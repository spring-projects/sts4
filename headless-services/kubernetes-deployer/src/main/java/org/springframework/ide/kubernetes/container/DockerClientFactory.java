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
package org.springframework.ide.kubernetes.container;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.jaxrs.JerseyDockerCmdExecFactory;

public class DockerClientFactory {
	
	
	public DockerClient getDockerClient() throws Exception {

		Path dockerPath = getDockerConfigPath();
		DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
				.withDockerConfig(dockerPath.toString()).build();

		JerseyDockerCmdExecFactory exec = new JerseyDockerCmdExecFactory().withConnectTimeout(1000)
				.withMaxTotalConnections(100).withMaxPerRouteConnections(10);
		return DockerClientBuilder.getInstance(config).withDockerCmdExecFactory(exec).build();

	}
	
	private Path getDockerConfigPath() throws Exception {
		String userHome = System.getProperty("user.home");
		Path dockerPath = null;
		if (StringUtils.hasText(userHome)) {
			dockerPath = Paths.get(userHome, ".docker");
			if (!java.nio.file.Files.exists(dockerPath)) {
				dockerPath = null;
			}
		}
		if (dockerPath == null) {
			throw new Exception(
					"Unable to find docker config file in user home. Ensure Docker is connected and config file accessible.");
		}
		return dockerPath;
	}

}
