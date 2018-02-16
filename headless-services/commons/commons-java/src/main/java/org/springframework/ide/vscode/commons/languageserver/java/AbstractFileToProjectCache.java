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
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.Sts4LanguageServer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Cache for java projects. The key for the cache is a "project" specific file
 * 
 * @author Alex Boyko
 *
 * @param <P> java project sub-class
 */
public abstract class AbstractFileToProjectCache<P extends IJavaProject> extends AbstractJavaProjectCache<File, P> {
	
	private static final Supplier<Logger> LOG = Suppliers.memoize(() -> LoggerFactory.getLogger(AbstractFileToProjectCache.class)); 
	
	private List<String> subscriptions;	
	protected boolean asyncUpdate;
	protected final Path projectCacheFolder;
	private boolean alwaysFireEventOnUpdate;

	private static AtomicInteger progressIdCt = new AtomicInteger(0);

	public AbstractFileToProjectCache(Sts4LanguageServer server, boolean asyncUpdate, Path projectCacheFolder) {
		super(server);
		this.projectCacheFolder = projectCacheFolder;
		this.asyncUpdate = asyncUpdate;
		this.subscriptions = new ArrayList<>();
	}
	
	
	final public void setAlwaysFireEventOnFileChanged(boolean alwaysFireEventOnUpdate) {
		this.alwaysFireEventOnUpdate = alwaysFireEventOnUpdate;
	}

	@Override
	protected void attachListeners(File file, P project) {
		super.attachListeners(file, project);
		List<String> globPattern = Arrays.asList(file.toString().replace(File.separator, "/"));
		subscriptions.add(getFileObserver().onFileChanged(globPattern, (uri) -> performUpdate(project, asyncUpdate, true)));
		subscriptions.add(getFileObserver().onFileDeleted(globPattern, (uri) -> {
			cache.invalidate(file);
			notifyProjectDeleted(project);
			dispose();
		}));
		
		Path outputFolder = project.getClasspath().getOutputFolder();
		if (outputFolder != null) {
			final List<String> rebuildGlobPattern = Arrays.asList(outputFolder.toString().replace(File.separator, "/") + "/**/*.class");
			subscriptions.add(getFileObserver().onFileChanged(rebuildGlobPattern, (uri) -> project.getClasspath().reindex()));
			subscriptions.add(getFileObserver().onFileCreated(rebuildGlobPattern, (uri) -> project.getClasspath().reindex()));
			subscriptions.add(getFileObserver().onFileDeleted(rebuildGlobPattern, (uri) -> project.getClasspath().reindex()));
		}
	}
	
	private void dispose() {
		subscriptions.forEach(s -> getFileObserver().unsubscribe(s));
		subscriptions.clear();
	}
	
	final protected void performUpdate(P project, boolean async, boolean notify) {
		final String taskId = getProgressId();
		final ProgressService progressService = server.getProgressService();
		if (progressService != null) {
			progressService.progressEvent(taskId, "Updating data for project `" + project.getElementName() + "'");
		}
		if (async) {
			LOG.get().info("Started updating project " + project.getElementName() + " in ASYNC mode");
			CompletableFuture.supplyAsync(() -> update(project)).thenAccept((changed) -> afterUpdate(project, changed, notify, taskId));
			LOG.get().info("Finished updating project " + project.getElementName() + " in ASYNC mode");
		} else {
			LOG.get().info("Started updating project " + project.getElementName() + " in SYNC mode");
			boolean changed = update(project);
			afterUpdate(project, changed, notify, taskId);
			LOG.get().info("Finished updating project " + project.getElementName() + " in SYNC mode");
		}
	}
	
	private void afterUpdate(P project, boolean changed, boolean notify, String taskId) {
		final ProgressService progressService = server.getProgressService();
		if (progressService != null) {
			progressService.progressEvent(taskId, null);
		}
		if (changed || alwaysFireEventOnUpdate) {
			if (notify) {
				notifyProjectChanged(project);
			}
		}
	}
	
	private static String getProgressId() {
		return AbstractFileToProjectCache.class.getName()+ (progressIdCt.incrementAndGet());
	}

	
	abstract protected boolean update(P project);
	
}
