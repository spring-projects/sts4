/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.maven;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;

public class MavenBuilder {


	private Path projectPath;

	private List<String> targets;

	private List<String> properties;

	public void execute() throws IOException, InterruptedException, TimeoutException {
		Path mvnwPath = System.getProperty("os.name").toLowerCase().startsWith("win") ? projectPath.resolve("mvnw.cmd")
				: projectPath.resolve("mvnw");
		Assert.isLegal(mvnwPath.toFile().isFile(), "No maven wrapper found at: "+mvnwPath);
		mvnwPath.toFile().setExecutable(true);
		List<String> all = new ArrayList<>(1 + targets.size() + properties.size());
		all.add(mvnwPath.toAbsolutePath().toString());
		all.addAll(targets);
		all.addAll(properties);
		ExternalProcess process = new ExternalProcess(projectPath.toFile(),
				new ExternalCommand(all.toArray(new String[all.size()])), true);
		if (process.getExitValue() != 0) {
			System.err.println("Failed to build test project!");
			System.err.println(process);
			throw new RuntimeException("Failed to build test project! " + process);
		}
	}

	public static MavenBuilder newBuilder(Path projectPath) {
		return new MavenBuilder(projectPath);
	}

	public MavenBuilder clean() {
		targets.add("clean");
		return this;
	}

	public MavenBuilder pack() {
		targets.add("package");
		return this;
	}

	public MavenBuilder skipTests() {
		properties.add("-DskipTests");
		return this;
	}

	public MavenBuilder javadoc() {
		properties.add("javadoc:javadoc");
		properties.add("-Dshow=private");
		return this;
	}

	private MavenBuilder(Path projectPath) {
		this.projectPath = projectPath;
		this.targets = new ArrayList<>();
		this.properties = new ArrayList<>();
	}

}
