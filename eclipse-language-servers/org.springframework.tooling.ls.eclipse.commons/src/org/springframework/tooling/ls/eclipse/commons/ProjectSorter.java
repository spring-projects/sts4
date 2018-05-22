/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

public class ProjectSorter implements Comparator<IProject> {

	private IProject activeProject;
	private Set<IProject> projectsWithEditors;

	public ProjectSorter() {

		this.projectsWithEditors = new HashSet<IProject>();

		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				IEditorInput editorInput = activeEditor.getEditorInput();
				activeProject = getProject(editorInput);

				IWorkbenchPage[] pages = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPages();
				for (IWorkbenchPage page : pages) {
					IEditorReference[] references = page.getEditorReferences();
					for (IEditorReference editorReference : references) {
						try {
							IEditorInput additionalInput = editorReference.getEditorInput();
							IProject additionalProject = getProject(additionalInput);

							if (additionalProject != null && additionalProject != activeProject) {
								projectsWithEditors.add(additionalProject);
							}
						} catch (PartInitException e) {
							// ignore this editor
						}
					}
				}
			}
		});
	}

	private IProject getProject(IEditorInput editorInput) {
		if (editorInput != null && editorInput instanceof FileEditorInput) {
			FileEditorInput fileInput = (FileEditorInput) editorInput;
			return fileInput.getFile() != null ? fileInput.getFile().getProject() : null;
		}
		else {
			return null;
		}
	}

	@Override
	public int compare(IProject o1, IProject o2) {
		return score(o2) - score(o1);
	}

	private int score(IProject project) {
		if (project == null) return 0;

		if (project == activeProject) return 10;
		if (projectsWithEditors.contains(project)) return 5;

		return 0;
	}

}
