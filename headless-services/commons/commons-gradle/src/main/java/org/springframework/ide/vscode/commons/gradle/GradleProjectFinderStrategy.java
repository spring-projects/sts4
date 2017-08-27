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
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.util.FileUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Tests whether document belongs to a Gradle project
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectFinderStrategy implements IJavaProjectFinderStrategy {

	private Cache<File, GradleJavaProject> cache = CacheBuilder.newBuilder().build();
	
	private GradleCore gradle;
	
	public GradleProjectFinderStrategy(GradleCore gradle) {
		this.gradle = gradle;
	}

	@Override
	public GradleJavaProject find(File file) throws ExecutionException {
		File gradlebuild = FileUtils.findFile(file, GradleCore.GRADLE_BUILD_FILE);
		if (gradlebuild != null) {
			return cache.get(gradlebuild.getParentFile(), () -> {
				return new GradleJavaProject(gradle, gradlebuild.getParentFile());
			});
		}
		return null;
	}

	@Override
	public boolean isProjectRoot(File file) {
		return FileUtils.findFile(file, GradleCore.GRADLE_BUILD_FILE, false) != null;
	}

}
