/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;
import java.net.URI;
import java.util.List;

import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;

public abstract class BootDiagnosticProvider {
	
	public static final String BOOT_VERSION_VALIDATION_CODE = "BOOT_VERSION_VALIDATION_CODE";
	private static final String[] BUILD_FILES = new String[] {"pom.xml", "build.gradle",  "build.gradle.kts"};
	
	/**
	 * 
	 * @param javaProject
	 * @param info Spring Dependency Info
	 * @param Spring project Generation information
	 * @return Diagnostic if applicable to the given version, or null
	 */
	abstract SpringProjectDiagnostic getDiagnostic(IJavaProject javaProject, SpringDependencyInfo info, Generations generations) throws Exception;
	
	protected URI getBuildFileUri(IJavaProject javaProject) throws Exception {

		File file = null;
		for (String fileName : BUILD_FILES) {
			file = SpringProjectUtil.getFile(javaProject, fileName);
			if (file != null) {
				return file.toURI();
			}
		}

		return null;
	}
	
	protected File getSpringBootDependency(IJavaProject project) {
		List<File> libs = SpringProjectUtil.getLibrariesOnClasspath(project, "spring-boot");
		return libs != null && libs.size() > 0 ? libs.get(0) : null;
	}

	protected Version getVersion(Generation generation) throws Exception {
		return SpringProjectUtil.getVersionFromGeneration(generation.getName());
	}
}
