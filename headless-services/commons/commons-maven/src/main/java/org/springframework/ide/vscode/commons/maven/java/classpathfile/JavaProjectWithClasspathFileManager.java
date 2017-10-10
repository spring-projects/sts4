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
package org.springframework.ide.vscode.commons.maven.java.classpathfile;

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

public class JavaProjectWithClasspathFileManager extends AbstractJavaProjectManager {

	private Cache<File, JavaProjectWithClasspathFile> cache = CacheBuilder.newBuilder().build();
	
	@Override
	public JavaProjectWithClasspathFile find(File file) {
		File cpFile = FileUtils.findFile(file, MavenCore.CLASSPATH_TXT);
		if (cpFile != null) {
			try {
				return cache.get(cpFile, () -> {
					return new JavaProjectWithClasspathFile(cpFile);
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
		return FileUtils.findFile(file, MavenCore.CLASSPATH_TXT, false) != null;
	}

	@Override
	public void setFileObserver(FileObserver fileObserver) {
		super.setFileObserver(fileObserver);
		cache.invalidateAll();
	}

	@Override
	protected FileListener createFileListener() {
		return new FileListener() {

			@Override
			public boolean accept(String uri) {
				return MavenCore.CLASSPATH_TXT.equals(Paths.get(URI.create(uri)).getFileName().toString());
			}
			
			private File getFileFromUri(String uri) {
				return Paths.get(URI.create(uri)).toFile();
			}
			
			@Override
			public void deleted(String uri) {
				File cpFile = getFileFromUri(uri);
				JavaProjectWithClasspathFile project = cache.getIfPresent(cpFile);
				if (project != null) {
					// Only invalidate and fire event if projects was cached.
					cache.invalidate(cpFile);
					notifyProjectDeleted(project);
				}
			}
			
			@Override
			public void created(String uri) {
				// Ignore created projects - cache should take care of newly created ones
			}
			
			@Override
			public void changed(String uri) {
				File cpFile = getFileFromUri(uri);
				JavaProjectWithClasspathFile project = cache.getIfPresent(cpFile);
				if (project != null) {
					// Only update and fire event if project cached
					project.update(cpFile);
					notifyProjectChanged(project);
				}
			}

			
		};
	}

}
