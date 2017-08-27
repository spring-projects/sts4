/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

import java.io.File;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.FileUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Finds Maven Project based
 *
 * @author Alex Boyko
 */
public class MavenProjectFinderStrategy implements IJavaProjectFinderStrategy {

	private Cache<File, MavenJavaProject> cache = CacheBuilder.newBuilder().build();
	
	private MavenCore maven;
	
	public MavenProjectFinderStrategy(MavenCore maven) {
		this.maven = maven;
	}

	@Override
	public MavenJavaProject find(File file) throws ExecutionException {
		File pomFile = FileUtils.findFile(file, MavenCore.POM_XML);
		if (pomFile != null) {
			return cache.get(pomFile, () -> {
				return new MavenJavaProject(maven, pomFile);
			});
		}
		return null;
	}

	@Override
	public boolean isProjectRoot(File file) {
		return FileUtils.findFile(file, MavenCore.POM_XML, false) != null;
	}

}
