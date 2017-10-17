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

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.FileBasedJavaProjectFinder;
import org.springframework.ide.vscode.commons.util.FileUtils;

/**
 * Finder for Gradle Projects
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectFinder extends FileBasedJavaProjectFinder {
	
	private GradleProjectCache cache;
	
	public GradleProjectFinder(GradleProjectCache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public IJavaProject find(File file) {
		File gradlebuild = FileUtils.findFile(file, GradleCore.GRADLE_BUILD_FILE);
		return cache.project(gradlebuild);
	}
}
