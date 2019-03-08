/*******************************************************************************
 * Copyright (c) 2014, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.boot.metadata.util.Listener;
import org.springframework.ide.vscode.boot.metadata.util.ListenerManager;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

/**
 * Support for Reconciling, Content Assist and Hover Text in spring properties
 * file all make use of a per-project index of spring properties metadata extracted
 * from project's classpath. This Index manager is responsible for keeping at most
 * one index per-project and to keep the index up-to-date.
 *
 * @author Kris De Volder
 */
public class SpringPropertiesIndexManager extends ListenerManager<Listener<SpringPropertiesIndexManager>> {

	private Cache<IJavaProject, SpringPropertyIndex> indexes;
	private final ValueProviderRegistry valueProviders;
	private static int progressIdCt = 0;

	public SpringPropertiesIndexManager(ValueProviderRegistry valueProviders, ProjectObserver projectObserver, FileObserver fileObserver) {
		this.valueProviders = valueProviders;
		this.indexes = CacheBuilder.newBuilder()
				.build();
		if (projectObserver != null) {
			projectObserver.addListener(ProjectObserver.onAny(project -> indexes.invalidate(project)));
		}
		if (fileObserver!=null) {
			fileObserver.onAnyChange(ImmutableList.of("**/*spring-configuration-metadata.json"), changed -> {
				clear();
			});
		}
	}

	public synchronized SpringPropertyIndex get(IJavaProject project, ProgressService progressService) {
		try {
			return indexes.get(project, () -> initIndex(project, progressService));
		} catch (ExecutionException e) {
			Log.log(e);
			return null;
		}
	}

	private SpringPropertyIndex initIndex(IJavaProject project, ProgressService progressService) {
		Log.info("Indexing Spring Boot Properties for "+project.getElementName());

		String progressId = getProgressId();
		if (progressService != null) {
			progressService.progressEvent(progressId, "Indexing Spring Boot Properties...");
		}

		SpringPropertyIndex index = new SpringPropertyIndex(valueProviders, project.getClasspath());

		if (progressService != null) {
			progressService.progressEvent(progressId, null);
		}

		Log.info("Indexing Spring Boot Properties for "+project.getElementName()+" DONE");
		Log.info("Indexed "+index.size()+" properties.");

		return index;
	}

	public synchronized void clear() {
		if (indexes!=null) {
			indexes.invalidateAll();
			for (Listener<SpringPropertiesIndexManager> l : getListeners()) {
				l.changed(this);
			}
		}
	}

	private static synchronized String getProgressId() {
		return DefaultSpringPropertyIndexProvider.class.getName()+ (progressIdCt++);
	}

}
