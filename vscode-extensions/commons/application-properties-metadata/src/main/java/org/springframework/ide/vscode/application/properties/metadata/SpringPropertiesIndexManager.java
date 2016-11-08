/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.application.properties.metadata;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ide.vscode.application.properties.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.application.properties.metadata.util.Listener;
import org.springframework.ide.vscode.application.properties.metadata.util.ListenerManager;
import org.springframework.ide.vscode.commons.java.IJavaProject;

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
	final private ValueProviderRegistry valueProviders;

	public SpringPropertiesIndexManager(ValueProviderRegistry valueProviders) {
		this.valueProviders = valueProviders;
	}

	public synchronized FuzzyMap<PropertyInfo> get(IJavaProject project) {
		if (indexes==null) {
			indexes = new HashMap<>();
		}
		SpringPropertyIndex index = indexes.get(project);
		if (index==null) {
			index = new SpringPropertyIndex(valueProviders, project.getClasspath());
			indexes.put(project, index);
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
	
}
