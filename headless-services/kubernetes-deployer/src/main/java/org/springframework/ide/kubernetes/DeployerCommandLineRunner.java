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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.ide.kubernetes.deployer.DeployRunner;
import org.springframework.ide.kubernetes.deployer.DeployerArgsParser;
import org.springframework.ide.kubernetes.deployer.DeploymentDefinition;

public class DeployerCommandLineRunner implements CommandLineRunner {

	private final DeployerArgsParser argsParser;

	private final DeployRunner runner;

	@Autowired
	public DeployerCommandLineRunner(DeployerArgsParser argsParser, DeployRunner runner) {
		this.argsParser = argsParser;
		this.runner = runner;
	}

	@Override
	public void run(String... args) throws Exception {

		DeploymentDefinition definition = argsParser.toDefinition(args);

		runner.run(definition);
	}

}
