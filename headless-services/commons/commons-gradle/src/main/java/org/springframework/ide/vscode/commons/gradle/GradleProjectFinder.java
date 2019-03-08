/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

	private static List<PathMatcher> PATH_MATCHERS = Arrays.asList(
			FileSystems.getDefault().getPathMatcher("glob:**/" + GradleCore.GRADLE_BUILD_FILE),
			FileSystems.getDefault().getPathMatcher("glob:" + GradleCore.GLOB_GRADLE_FILE)
		);

	private GradleProjectCache cache;

	public GradleProjectFinder(GradleProjectCache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public Optional<IJavaProject> find(File file) {
		return FileUtils.findFile(file, PATH_MATCHERS, true).map(f -> cache.project(f));
	}

	@Override
	protected Optional<IJavaProject> findProjectByName(String name) {
		return cache.projectByName(name);
	}

	@Override
	public Collection<? extends IJavaProject> all() {
		return cache.all();
	}
}
