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
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.java.AbstractJavaProjectManager;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.FileObserver.FileListener;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Tests whether document belongs to a Gradle project
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectManager extends AbstractJavaProjectManager {

	private Cache<File, GradleJavaProject> cache = CacheBuilder.newBuilder().build();
	
	private GradleCore gradle;
	
	public GradleProjectManager(GradleCore gradle) {
		this.gradle = gradle;
	}

	@Override
	public GradleJavaProject find(File file) {
		File gradlebuild = FileUtils.findFile(file, GradleCore.GRADLE_BUILD_FILE);
		if (gradlebuild != null) {
			try {
				return cache.get(gradlebuild.getParentFile(), () -> {
					System.out.println("create gradle project: " + file.getAbsolutePath());
					GradleJavaProject project = new GradleJavaProject(gradle, gradlebuild.getParentFile());
					System.out.println("gradle project created");
					return project;
				});
			} catch (ExecutionException e) {
				Log.log(e);
				return null;
			}
		}
		return null;
	}

	@Override
	public boolean isProjectRoot(File file) {
		return FileUtils.findFile(file, GradleCore.GRADLE_BUILD_FILE, false) != null;
	}
	
	

	@Override
	public void setFileObserver(FileObserver fileObserver) {
		super.setFileObserver(fileObserver);
		cache.invalidateAll();
	}

	@Override
	protected FileListener createFileListener() {
		return new FileListener() {
			
			private File getProjectFolderFromUri(String uri) {
				return Paths.get(URI.create(uri)).toFile().getParentFile();
			}
			
			@Override
			public void deleted(String uri) {
				File gradleProjectFolder = getProjectFolderFromUri(uri);
				GradleJavaProject project = cache.getIfPresent(gradleProjectFolder);
				if (project != null) {
					cache.invalidate(gradleProjectFolder);
					notifyProjectDeleted(project);
				}
			}
			
			@Override
			public void created(String uri) {
				// Ignore created projects - cache should take care of newly created ones
			}
			
			@Override
			public void changed(String uri) {
				File gradleProjectFolder = getProjectFolderFromUri(uri);
				GradleJavaProject project = cache.getIfPresent(gradleProjectFolder);
				if (project != null) {
					project.update(gradle, gradleProjectFolder);
					notifyProjectChanged(project);
				}
			}
			
			@Override
			public boolean accept(String uri) {
				return GradleCore.GRADLE_BUILD_FILE.equals(Paths.get(URI.create(uri)).getFileName().toString());
			}
		};
	}

}
