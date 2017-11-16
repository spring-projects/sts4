/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.java.AbstractJavaProject;
import org.springframework.ide.vscode.commons.java.DelegatingCachedClasspath;

/**
 * Implementation of Gradle Java project
 * 
 * @author Alex Boyko
 *
 */
public class GradleJavaProject extends AbstractJavaProject {
	
	private DelegatingCachedClasspath<GradleProjectClasspath> classpath;
	private File projectDir;
	
	public GradleJavaProject(GradleCore gradle, File projectDir, Path projectDataCache) {
		super(projectDataCache);
		this.projectDir = projectDir;
		this.classpath = new DelegatingCachedClasspath<GradleProjectClasspath>(
				() -> new GradleProjectClasspath(gradle, projectDir),
				projectDataCache == null ? null : projectDataCache.resolve(DelegatingCachedClasspath.CLASSPATH_DATA_CACHE_FILE).toFile()
			);
	}
	
	public File getLocation() {
		return projectDir;
	}

	@Override
	public DelegatingCachedClasspath<GradleProjectClasspath> getClasspath() {
		return classpath;
	}
	
	boolean update() {
		return classpath.update();
	}
	

}
