/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.project.harness;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;

/**
 * Test project harness utilities
 * 
 * @author Alex Boyko
 *
 */
public class ProjectsHarness {
	
	/**
	 * Builds maven project
	 * 
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public static Path buildMavenProject(String name) throws Exception {
		Path testProjectPath = Paths.get(ProjectsHarness.class.getResource("/" + name).toURI());
		if (!Files.exists(testProjectPath.resolve("classpath.txt"))) {
			Path mvnwPath = System.getProperty("os.name").toLowerCase().startsWith("win")
					? testProjectPath.resolve("mvnw.cmd") : testProjectPath.resolve("mvnw");
			mvnwPath.toFile().setExecutable(true);
			ExternalProcess process = new ExternalProcess(testProjectPath.toFile(),
					new ExternalCommand(mvnwPath.toAbsolutePath().toString(), "clean", "package"), true);
			if (process.getExitValue() != 0) {
				throw new RuntimeException("Failed to build test project");
			}
		}
		return testProjectPath;
	}

}
