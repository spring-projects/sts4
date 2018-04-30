/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.java.AbstractJavaProject;
import org.springframework.ide.vscode.commons.java.ClasspathFileBasedCache;
import org.springframework.ide.vscode.commons.java.DelegatingCachedClasspath;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Wrapper for Maven Core project
 * 
 * @author Alex Boyko
 *
 */
public class MavenJavaProject extends AbstractJavaProject {
	
	private final File pom;

	private MavenJavaProject(FileObserver fileObserver, Path projectDataCache, IClasspath classpath, File pom) {
		super(fileObserver, pom.getParentFile().toURI(), projectDataCache, classpath);
		this.pom = pom;
	}

	public static MavenJavaProject create(FileObserver fileObserver, MavenCore maven, File pom, Path projectDataCache) {
		File file = projectDataCache == null 
				? null
				: projectDataCache.resolve(ClasspathFileBasedCache.CLASSPATH_DATA_CACHE_FILE).toFile();
		ClasspathFileBasedCache fileBasedCache = new ClasspathFileBasedCache(file);
		DelegatingCachedClasspath classpath = new DelegatingCachedClasspath(
				() -> new MavenProjectClasspath(maven, pom),
				fileBasedCache 
		);
		return new MavenJavaProject(fileObserver, projectDataCache, classpath, pom);
	}
	
	public static MavenJavaProject create(FileObserver fileObserver, MavenCore maven, File pom) {
		MavenJavaProject thiss = create(fileObserver, maven, pom, null);
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
	public String getElementName() {
		if (getClasspath().getName() == null) {
			return pom.getParentFile().getName();
		} else {
			return super.getElementName();
		}
	}
	
	@Override
	public DelegatingCachedClasspath getClasspath() {
		return (DelegatingCachedClasspath) super.getClasspath();
	}
	
	boolean update() throws Exception {
		return getClasspath().update();
	}
	
	public File pom() {
		return pom;
	}
	
	@Override
	public String toString() {
		return "MavenJavaProject("+getElementName()+")";
	}
}
