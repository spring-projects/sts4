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
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Cache for java projects. The key for the cache is a "project" specific file
 * 
 * @author Alex Boyko
 *
 * @param <P> java project sub-class
 */
public abstract class AbstractFileToProjectCache<P extends IJavaProject> extends AbstractJavaProjectCache<File, P> {
	
	private String changeSubscription;
	private String deleteSubscription;
	protected boolean asyncUpdate;
	protected final Path projectCacheFolder;
	private boolean alwaysFireEventOnFileChanged;

	public AbstractFileToProjectCache(FileObserver fileObserver, boolean asyncUpdate, Path projectCacheFolder) {
		super(fileObserver);
		this.projectCacheFolder = projectCacheFolder;
		this.asyncUpdate = asyncUpdate;
	}
	
	
	final public void setAlwaysFireEventOnFileChanged(boolean alwaysFireEventOnFileChanged) {
		this.alwaysFireEventOnFileChanged = alwaysFireEventOnFileChanged;
	}

	@Override
	protected void attachListeners(File file, P project) {
		super.attachListeners(file, project);
		List<String> globPattern = Arrays.asList(file.toString());
		changeSubscription = getFileObserver().onFileChanged(globPattern, (uri) -> performUpdate(project, asyncUpdate));
		deleteSubscription = getFileObserver().onFileDeleted(globPattern, (uri) -> {
			cache.invalidate(file);
			notifyProjectDeleted(project);
			getFileObserver().unsubscribe(changeSubscription);
			getFileObserver().unsubscribe(deleteSubscription);
		});
	}
	
	final protected void performUpdate(P project, boolean async) {
		if (async) {
			CompletableFuture.supplyAsync(() -> update(project)).thenAccept((changed) -> {
				if (changed || alwaysFireEventOnFileChanged) {
					notifyProjectChanged(project);
				}
			});
		} else {
			if (update(project) || alwaysFireEventOnFileChanged) {
				notifyProjectChanged(project);
			}
		}
	}
	
	abstract protected boolean update(P project);
	
}
