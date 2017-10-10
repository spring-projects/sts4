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
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.java.AbstractJavaProjectManager;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.FileObserver.FileListener;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Finds Maven Project based
 *
 * @author Alex Boyko
 */
public class MavenProjectManager extends AbstractJavaProjectManager {

	private Cache<File, MavenJavaProject> cache = CacheBuilder.newBuilder().build();
	
	private MavenCore maven;
	
	public MavenProjectManager(MavenCore maven) {
		this.maven = maven;
	}

	@Override
	public MavenJavaProject find(File file) {
		File pomFile = FileUtils.findFile(file, MavenCore.POM_XML);
		if (pomFile != null) {
			try {
				return cache.get(pomFile, () -> {
					System.out.println("create maven project: " + file.getAbsolutePath());
					MavenJavaProject project = new MavenJavaProject(maven, pomFile);
					System.out.println("maven project created");
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
		return FileUtils.findFile(file, MavenCore.POM_XML, false) != null;
	}
	
	@Override
	public void setFileObserver(FileObserver fileObserver) {
		super.setFileObserver(fileObserver);
		cache.invalidateAll();
	}

	@Override
	protected FileListener createFileListener() {
		return new FileListener() {
			
			private File getFileFromUri(String uri) {
				return Paths.get(URI.create(uri)).toFile();
			}
			
			@Override
			public void deleted(String uri) {
				File pomFile = getFileFromUri(uri);
				MavenJavaProject project = cache.getIfPresent(pomFile);
				if (project != null) {
					// Only invalidate and fire event if projects was cached.
					cache.invalidate(pomFile);
					notifyProjectDeleted(project);
				}
			}
			
			@Override
			public void created(String uri) {
				// Ignore created projects - cache should take care of newly created ones
			}
			
			@Override
			public void changed(String uri) {
				File pomFile = getFileFromUri(uri);
				MavenJavaProject project = cache.getIfPresent(pomFile);
				if (project != null) {
					// Only update and fire event if project cached
					project.update(maven, pomFile);
					notifyProjectChanged(project);
				}
			}
			
			@Override
			public boolean accept(String uri) {
				return MavenCore.POM_XML.equals(Paths.get(URI.create(uri)).getFileName().toString());
			}
		};
	}

}
