/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard;

import java.util.List;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.ProjectFilter;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.utils.SelectionUtils;

public class StartersWizardUtil {

	private static final String AUTO_SAVE_DIRTY_POM = "EditStartersDialog.pom.autosave";
	private static final int SAVE_BUTTON = 256;

	public static IProject getProject(IStructuredSelection selection) {
		List<IProject> projects = SelectionUtils.getProjects(selection, ProjectFilter.anyProject);
		if (projects != null && !projects.isEmpty()) {
			return projects.get(0);
		}
		return null;
	}

	public static int promptIfPomFileDirty(IProject selectedProject, Shell shell) throws CoreException {
		ITextFileBuffer dirtyPom = getDirtyPomFile(selectedProject);
		if (dirtyPom != null) {
			if (!savePom(selectedProject, shell, dirtyPom)) {
				return MessageDialog.CANCEL;
			}
		}
		return MessageDialog.OK;

	}

	/**
	 * Ask the user if its okay to save the pom. If they say yes, save it.
	 * <p>
	 *
	 * @return true if the pom was saved.
	 */
	private static boolean savePom(IProject selectedProject, Shell shell, ITextFileBuffer dirtyPom)
			throws CoreException {
		IPreferenceStore prefs = BootActivator.getDefault().getPreferenceStore();
		boolean autoSave = prefs.getBoolean(AUTO_SAVE_DIRTY_POM);
		boolean save = autoSave || askSavePom(selectedProject, shell, dirtyPom);
		if (save) {
			dirtyPom.commit(new NullProgressMonitor(), true);
		}
		return save;
	}

	private static boolean askSavePom(IProject selectedProject, Shell shell, ITextFileBuffer dirtyPom)
			throws CoreException {
		MessageDialogWithToggle dlg = new MessageDialogWithToggle(shell, "Pom file needs saving!", null,
				"The pom file for project '" + selectedProject.getName()
						+ "' has unsaved edits. The 'Add Starters' dialog makes changes to "
						+ "the pom file on disk. Do you want to save it now?",
				MessageDialog.WARNING, new String[] { "Cancel", "Save Pom" }, 1,
				"Don't ask again and save automatically in the future", false);
		int code = dlg.open();
		if (code == SAVE_BUTTON) { // The Dialog has a weird logic but that's the number it gets for our 'save'
									// button based on its position.
			if (dlg.getToggleState()) {
				BootActivator.getDefault().getPreferenceStore().setValue(AUTO_SAVE_DIRTY_POM, true);
			}
			return true;
		}
		return false;
	}

	private static ITextFileBuffer getDirtyPomFile(IProject project) {
		if (project.isAccessible()) {
			IFile pomFile = project.getFile("pom.xml");
			ITextFileBuffer buffer = FileBuffers.getTextFileBufferManager().getTextFileBuffer(pomFile.getFullPath(),
					LocationKind.IFILE);
			if (buffer != null && buffer.isDirty()) {
				return buffer;
			}
		}
		return null;
	}

}
