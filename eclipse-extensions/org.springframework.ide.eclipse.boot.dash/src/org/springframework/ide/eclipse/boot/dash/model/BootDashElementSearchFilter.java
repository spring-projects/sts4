/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

/**
 * Boot Dash elements filter working on strings from BDEs tags combined with
 * working set names (if BDE element delegates to a project in the workspace
 *
 * @author Alex Boyko
 *
 */
public class BootDashElementSearchFilter extends TagSearchFilter<BootDashElement> {

	private Map<IProject, Set<String>> mapping;

	public BootDashElementSearchFilter(String s) {
		super(s);
	}

	private Map<IProject, Set<String>> getMapping() {
		if (mapping == null) {
			mapping = new HashMap<>();
			for (IWorkingSet ws : PlatformUI.getWorkbench().getWorkingSetManager().getAllWorkingSets()) {
				if (!ws.isAggregateWorkingSet()) {
					for (IAdaptable a : ws.getElements()) {
						IProject project = (IProject)a.getAdapter(IProject.class);
						if (project != null) {
							Set<String> set = mapping.get(project);
							if (set == null) {
								set = new HashSet<>();
								mapping.put(project, set);
							}
							set.add(ws.getName());
						}
					}
				}
			}
		}
		return mapping;
	}

	@Override
	protected ImmutableSet<String> getTags(BootDashElement element) {
		Builder<String> tags = ImmutableSet.builder();
		tags.addAll(super.getTags(element));
		// Add implicit tag for element name
		tags.add(element.getName());
		// Add implicit tags for Working Sets
		if (element.getProject() != null) {
			Set<String> workingSetNames = getMapping().get(element.getProject());
			if (workingSetNames != null) {
				tags.addAll(workingSetNames);
			}
		}
		return tags.build();
	}

}
