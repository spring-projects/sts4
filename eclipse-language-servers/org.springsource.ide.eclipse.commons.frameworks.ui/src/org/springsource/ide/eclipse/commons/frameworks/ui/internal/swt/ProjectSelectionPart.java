/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.springsource.ide.eclipse.commons.frameworks.ui.FrameworkUIActivator;


/**
 * UI part that allows the selection of projects from a dropdown menu
 * <p/>
 * This part can be added to any JFace/SWT control that uses Grid layout, and
 * includes a pop-up dialogue that allows the user to confirm whether to proceed
 * with the switch or not.
 * <p/>
 * The pop-up dialogue is controlled preference that can be set from the
 * dialogue itself.
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public class ProjectSelectionPart extends ProjectSelector {

	private Combo projectList;

	private Collection<IProject> projects;
	private boolean showProjectSwitchDialogue;
	private IProjectSelectionHandler handler;

	public static final String DO_NOT_PROMPT_PROJECT_SWITCH = "DoNotPromptProjectSwitch";

	public ProjectSelectionPart(Shell shell, Composite parent,
			Collection<IProject> projects, IProjectSelectionHandler handler) {
		super(shell, parent);
		this.projects = projects;
		this.handler = handler;
	}

	public void showProjectSwitchDialogue(boolean show) {
		showProjectSwitchDialogue = show;
	}

	protected Collection<IProject> getProjects() {
		return projects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.springsource.sts.frameworks.ui.internal.swt.ProjectSelector#
	 * createProjectArea()
	 */
	public Composite createProjectArea() {
		Collection<IProject> availableProjects = getProjects();

		if (availableProjects == null || availableProjects.isEmpty()) {
			return null;
		}

		Composite parent = super.createProjectArea();

		String[] availableProjectNames = new String[availableProjects.size()];

		int i = 0;
		for (IProject project : availableProjects) {
			if (i < availableProjectNames.length) {
				availableProjectNames[i++] = project.getName();
			}
		}

		projectList = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		projectList.setItems(availableProjectNames);

		projectList.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				Widget widget = e.widget;
				if (widget == projectList) {
					String newSelection = projectList.getItem(projectList
							.getSelectionIndex());
					changeProject(newSelection);
				}
			}
		});

		// Set the default selection
		projectList.select(0);
		setSelectedProjectName(availableProjectNames[0]);
		return parent;
	}

	protected IProject changeProject(String projectNameToSwitch) {
		if (projectNameToSwitch == null) {
			return null;
		}

		int selectedIndex = -1;
		String[] availableProjects = projectList.getItems();
		for (int i = 0; i < availableProjects.length; i++) {
			if (projectNameToSwitch.equals(availableProjects[i])) {
				selectedIndex = i;
				break;
			}
		}

		// Not found in selection
		if (selectedIndex < 0) {
			return null;
		}

		if (showProjectSwitchDialogue
				&& !MessageDialogWithToggle.ALWAYS.equals(FrameworkUIActivator
						.getDefault().getPreferenceStore()
						.getString(DO_NOT_PROMPT_PROJECT_SWITCH))) {
			MessageDialogWithToggle dialogue = MessageDialogWithToggle
					.openYesNoCancelQuestion(
							getShell(),
							"Confirm Project Switch",
							"Switching to project: "
									+ projectNameToSwitch
									+ ". All current plugin changes will be lost. Do you wish to proceed?",
							"Do not show this dialogue again.", false,
							FrameworkUIActivator.getDefault()
									.getPreferenceStore(),
							DO_NOT_PROMPT_PROJECT_SWITCH);
			if (dialogue.getReturnCode() != IDialogConstants.YES_ID) {
				// Restore the selection in the project list
				projectList.select(getIndex(getSelectedProjectName()));
				return null;
			}
		}
		// Do the actual switch
		setSelectedProjectName(projectNameToSwitch);

		projectList.select(selectedIndex);

		IProject project = getSelectedProject();
		handleProjectChange(project);
		return project;
	}

	/**
	 * Get index of the given item, or -1 if it is not found.
	 * 
	 * @param name
	 * @return index of item, or -1 if not found
	 */
	protected int getIndex(String name) {
		if (projectList == null || name == null) {
			return -1;
		}

		String[] items = projectList.getItems();

		if (items == null) {
			return -1;
		}

		for (int i = 0; i < items.length; i++) {
			if (name.equals(items[i])) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns true if selection was successful. It returns false if project is
	 * already selected or could not be selected, either because it was not
	 * found or part is not yet created, or it is disposed.
	 * <p/>
	 * The part must be created first before manual selection of the project is
	 * possible.
	 * 
	 * @param project
	 * @return
	 */
	protected boolean selectProject(IProject project) {
		if (projectList == null || projectList.isDisposed() || project == null) {
			return false;
		}

		String currentSelection = getSelectedProjectName();
		String projectNameToSwitch = project.getName();
		if (currentSelection == null
				|| currentSelection.equals(projectNameToSwitch)) {
			return false;
		}
		return changeProject(projectNameToSwitch) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.frameworks.ui.internal.swt.ProjectSelector#setProject
	 * (org.eclipse.core.resources.IProject)
	 */
	public boolean setProject(IProject project) {
		//Do not call super.setProject *before* selectProject or selectProject will not update the UI
		// combo box accordingly (mistakenly thinks it is already set!)
		boolean result = selectProject(project);
		if (result) 
			super.setProject(project);
		return result;
	}

	/**
	 * Subclass override to implement specific logic for handling the new
	 * selected project
	 * 
	 * @param selectedProject
	 */
	protected void handleProjectChange(IProject selectedProject) {
		if (handler != null) {
			handler.handleProjectSelectionChange(selectedProject);
		}
	}

}
