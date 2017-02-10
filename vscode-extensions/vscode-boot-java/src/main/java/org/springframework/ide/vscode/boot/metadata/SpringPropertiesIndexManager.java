/*******************************************************************************
 * Copyright (c) 2014, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.boot.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.boot.metadata.util.Listener;
import org.springframework.ide.vscode.boot.metadata.util.ListenerManager;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;

/**
 * Support for Reconciling, Content Assist and Hover Text in spring properties
 * file all make use of a per-project index of spring properties metadata extracted
 * from project's classpath. This Index manager is responsible for keeping at most
 * one index per-project and to keep the index up-to-date.
 *
 * @author Kris De Volder
 */
public class SpringPropertiesIndexManager extends ListenerManager<Listener<SpringPropertiesIndexManager>> {

	private Map<IJavaProject, SpringPropertyIndex> indexes = null;
	private final ValueProviderRegistry valueProviders;
	private static int progressIdCt = 0;

	public SpringPropertiesIndexManager(ValueProviderRegistry valueProviders) {
		this.valueProviders = valueProviders;
	}

	public synchronized FuzzyMap<PropertyInfo> get(IJavaProject project, ProgressService progressService) {
		if (indexes==null) {
			indexes = new HashMap<>();
		}
		SpringPropertyIndex index = indexes.get(project);
		if (index==null) {
			String progressId = getProgressId();
			if (progressService != null) {
				progressService.progressEvent(progressId, "Indexing Spring Boot Properties...");
			}
			
			index = new SpringPropertyIndex(valueProviders, project.getClasspath());
			indexes.put(project, index);
			
			if (progressService != null) {
				progressService.progressEvent(progressId, null);
			}
		}
		return index;
	}

	public synchronized void clear() {
		if (indexes!=null) {
			indexes.clear();
			for (Listener<SpringPropertiesIndexManager> l : getListeners()) {
				l.changed(this);
			}
		}
	}
	
	private static synchronized String getProgressId() {
		return DefaultSpringPropertyIndexProvider.class.getName()+ (progressIdCt++);
	}

}
