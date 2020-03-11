/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.springframework.tooling.jdt.ls.commons.Logger;

public class ProjectSorter implements Comparator<IProject> {

	private Set<IProject> projectsWithEditors;

	private Logger logger = Logger.forEclipsePlugin(() -> LanguageServerCommonsActivator.getInstance());

	public ProjectSorter() {
		logger.log("Creating project sorter...");
		this.projectsWithEditors = new HashSet<IProject>();

		IFileBuffer[] openBuffers = FileBuffers.getTextFileBufferManager().getFileBuffers();
		for (IFileBuffer ob : openBuffers) {
			IPath path = ob.getLocation();
			System.out.println(path);
			if (path.segmentCount() >= 1) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
				projectsWithEditors.add(project);
			}
		}
		logger.log("Creating project sorter... DONE");
	}

	@Override
	public int compare(IProject o1, IProject o2) {
		return score(o2) - score(o1);
	}

	private int score(IProject project) {
		if (project == null) return 0;
		if (projectsWithEditors.contains(project)) return 5;
		return 0;
	}
}
