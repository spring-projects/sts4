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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.kubernetes.deployer.DeployRunner;
import org.springframework.ide.kubernetes.deployer.DeployerArgsParser;

@SpringBootApplication
public class DeployerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeployerApplication.class, args);
	}

	@Bean
	public DeployerCommandLineRunner getCommandLineRunner(DeployerArgsParser argsParser, DeployRunner runner) {
		return new DeployerCommandLineRunner(argsParser, runner);
	}
}
