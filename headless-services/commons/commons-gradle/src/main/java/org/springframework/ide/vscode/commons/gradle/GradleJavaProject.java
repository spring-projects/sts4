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
import org.springframework.ide.vscode.commons.java.ClasspathFileBasedCache;
import org.springframework.ide.vscode.commons.java.DelegatingCachedClasspath;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Implementation of Gradle Java project
 *
 * @author Alex Boyko
 *
 */
public class GradleJavaProject extends AbstractJavaProject {

	private GradleJavaProject(FileObserver fileObserver, Path projectDataCache, IClasspath classpath, File projectDir) {
		super(fileObserver, projectDir.toURI(), projectDataCache, classpath);
	}

	public static GradleJavaProject create(FileObserver fileObserver, GradleCore gradle, File projectDir, Path projectDataCache) {
		File file = projectDataCache == null
				? null
				: projectDataCache.resolve(ClasspathFileBasedCache.CLASSPATH_DATA_CACHE_FILE).toFile();
		ClasspathFileBasedCache fileBasedCache = new ClasspathFileBasedCache(file);
		IClasspath classpath = new DelegatingCachedClasspath(
				() -> new GradleProjectClasspath(gradle, projectDir),
				fileBasedCache
			);
		return new GradleJavaProject(fileObserver, projectDataCache, classpath, projectDir);
	}

	public static GradleJavaProject create(FileObserver fileObserver, GradleCore gradle, File projectDir) {
		GradleJavaProject thiss = create(fileObserver, gradle, projectDir, null);
		if (!thiss.getClasspath().isCached()) {
			try {
				thiss.getClasspath().update();
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return thiss;
	}

	@Override
	public DelegatingCachedClasspath getClasspath() {
		return (DelegatingCachedClasspath) super.getClasspath();
	}

	boolean update() throws Exception {
		return getClasspath().update();
	}

	@Override
	public String toString() {
		return "GradleJavaProject("+getElementName()+")";
	}

}
