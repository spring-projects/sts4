/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.exceptions;

import java.io.File;
import java.util.List;

import org.springframework.ide.eclipse.boot.dash.api.AppConsole;
import org.springframework.ide.eclipse.boot.dash.console.LogType;

/**
 * Exception raised when BootDash wants to build a docker image but can not
 * find / determine how to build it. I.e. we are looking for a number of
 * different build scripts in project-relative locations. If none of 
 * them are found then this exception is raised.
 */
public class MissingBuildScriptException extends DockerBuildException {
	
	private static final long serialVersionUID = 1L;
	
	public final List<File> locationsChecked;
	
	public MissingBuildScriptException(List<File> locationsChecked) {
		super("Neither maven wrapper, gradle wrapper or custom build script found.");
		this.locationsChecked = locationsChecked;
	}

	@Override
	public void writeDetailedExplanation(AppConsole console) throws Exception {
		console.write("Places we looked for build script: ", LogType.STDERROR);
		for (File loc : this.locationsChecked) {
			console.write(" - "+loc, LogType.STDERROR);
		}
		showBuildScriptHelp(console);
	}

	private void showBuildScriptHelp(AppConsole console) {
		String[] help = {
				"To build a docker image, Boot Dash needs to run a build script from your project.",
				"Three different types are supported and checked for in this order:",
				"",
				"1. "+this.locationsChecked.get(0).getAbsoluteFile().getName(),
				"   A custom script placed by you at the project root.",
				"   Typically this runs a custom maven or gradle command on your project.",
				"",
				"2. maven",
				"   If your project has a mvnw, we will use that to execute the `spring-boot:build-image` task",
				"",
				"3. gradle",
				"   If your project has a gradlew, we will use that to execute the `bootBuildImage` task",
		};
		try {
			for (String line : help) {
				console.write(line, LogType.STDERROR);
			}
		} catch (Exception e1) {
			//ignore
		}
	}



}
