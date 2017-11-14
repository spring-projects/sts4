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

import org.springframework.ide.vscode.commons.languageserver.java.AbstractFileToProjectCache;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Tests whether document belongs to a Gradle project
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectCache extends AbstractFileToProjectCache<GradleJavaProject> {

	private GradleCore gradle;
	
	public GradleProjectCache(FileObserver fileObserver, GradleCore gradle, boolean asyncUpdate, Path projectCacheFolder) {
		super(fileObserver, asyncUpdate, projectCacheFolder);
		this.gradle = gradle;
	}
	
	@Override
	protected boolean update(GradleJavaProject project) {
		return project.update();
	}

	@Override
	protected GradleJavaProject createProject(File gradleBuild) throws Exception {
		File gradleFile = gradleBuild.getParentFile();
		GradleJavaProject gradleJavaProject = new GradleJavaProject(gradle, gradleFile,
				projectCacheFolder == null ? null : gradleFile.toPath().resolve(projectCacheFolder));
		if (gradleJavaProject.getClasspath().isCached()) {
			performUpdate(gradleJavaProject, asyncUpdate);
		}
		return gradleJavaProject;
	}

}
