/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven.java.classpathfile;

import java.io.File;
import java.util.Optional;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.FileBasedJavaProjectFinder;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.FileUtils;

/**
 * Finder for projects with classpath.txt file that contains classpath entries
 * For test purposes only.
 * 
 * @author Alex Boyko
 *
 */
public class JavaProjectWithClasspathFileFinder extends FileBasedJavaProjectFinder {
	
	private JavaProjectWithClasspathFileCache cache;
	
	public JavaProjectWithClasspathFileFinder(JavaProjectWithClasspathFileCache cache) {
		super();
		this.cache = cache;
	}

	@Override
	public Optional<IJavaProject> find(File file) {
		File cpFile = FileUtils.findFile(file, MavenCore.CLASSPATH_TXT);
		if (cpFile!=null) {
			return Optional.ofNullable(cache.project(cpFile));
		}
		return Optional.empty();
	}

	@Override
	protected Optional<IJavaProject> findProjectByName(String name) {
		return cache.projectByName(name);
	}
}
