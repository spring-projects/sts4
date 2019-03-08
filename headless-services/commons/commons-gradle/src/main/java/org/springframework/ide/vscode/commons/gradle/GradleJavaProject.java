/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.ClasspathFileBasedCache;
import org.springframework.ide.vscode.commons.java.DelegatingCachedClasspath;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.LegacyJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavadocService;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Implementation of Gradle Java project
 *
 * @author Alex Boyko
 *
 */
public class GradleJavaProject extends LegacyJavaProject {

	private static final Logger log = LoggerFactory.getLogger(GradleJavaProject.class);

	private GradleJavaProject(FileObserver fileObserver, Path projectDataCache, IClasspath classpath, File projectDir, JavadocService javadocService) {
		super(fileObserver, projectDir.toURI(), projectDataCache, classpath, javadocService);
	}

	public static GradleJavaProject create(FileObserver fileObserver, GradleCore gradle, File projectDir, Path projectDataCache, JavadocService javadocService) {
		File file = projectDataCache == null
				? null
				: projectDataCache.resolve(ClasspathFileBasedCache.CLASSPATH_DATA_CACHE_FILE).toFile();
		ClasspathFileBasedCache fileBasedCache = new ClasspathFileBasedCache(file);
		IClasspath classpath = new DelegatingCachedClasspath(
				() -> new GradleProjectClasspath(gradle, projectDir),
				fileBasedCache
			);
		return new GradleJavaProject(fileObserver, projectDataCache, classpath, projectDir, javadocService);
	}

	public static GradleJavaProject create(FileObserver fileObserver, GradleCore gradle, File projectDir, JavadocService javadocService) {
		GradleJavaProject thiss = create(fileObserver, gradle, projectDir, null, javadocService);
		if (!thiss.getClasspath().isCached()) {
			try {
				thiss.getClasspath().update();
			} catch (Exception e) {
				log.error("", e);
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
