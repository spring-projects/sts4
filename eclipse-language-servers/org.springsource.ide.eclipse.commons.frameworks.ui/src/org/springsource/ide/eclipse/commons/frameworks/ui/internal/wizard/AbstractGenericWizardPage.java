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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.wizard;

import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.IProjectSelectionHandler;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.IProjectSelector;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ProjectSelectionPart;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.swt.ProjectSelectorFactory;


/**
 * Base page for all Generic command wizard pages. Among other things, it adds
 * project selection support for each page, and ensures each page displays the
 * same project.
 * @author Nieraj Singh
 * @author Kris De Volder
 */
public abstract class AbstractGenericWizardPage extends WizardPage {

	private IProjectSelector projectSelector;

	protected AbstractGenericWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Left and right width margin in pixels. Distance between edge of page and
	 * the control area.
	 * 
	 * @return
	 */
	protected int getPageWMargin() {
		return convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
	}

	/**
	 * Top and bottom margin in pixels. Distance between edge of page and the
	 * control area.
	 * 
	 * @return
	 */
	protected int getPageHMargin() {
		return convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
	}

	public void createControl(Composite parent) {
		setTitle(getTitle());
		setMessage(getMessage());
		
		initializeDialogUnits(parent);

		Composite wizardArea = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();

		wizardArea.setLayout(layout);

		createProjectSelectionArea(wizardArea);

		createPageArea(wizardArea);

		Dialog.applyDialogFont(wizardArea);

		setControl(wizardArea);
	}

	/**
	 * Sets a project selection in the wizard.
	 * 
	 * @param selectedProject
	 *           project to set in the wizard
	 */
	protected void setProjectInWizard(IProject selectedProject) {
		GenericCommandWizard commandWizard = getGenericCommandWizard();
		commandWizard.setSelectedProject(selectedProject);
	}

	protected GenericCommandWizard getGenericCommandWizard() {
		IWizard wizard = getWizard();
		if (wizard instanceof GenericCommandWizard) {
			return (GenericCommandWizard) wizard;
		}
		return null;
	}

	protected void setProjectSelectionInPage() {
		GenericCommandWizard wizard = getGenericCommandWizard();
		if (wizard != null) {

			IProject project = wizard.getSelectedProject();

			// If a selection already exists in the wizard, set it
			if (project != null && projectSelector != null) {
				projectSelector.setProject(project);
			}
		}
	}

	public IWizardPage getNextPage() {
		setProjectSelectionInPage();
		return super.getNextPage();
	}

	public IWizardPage getPreviousPage() {
		setProjectSelectionInPage();
		return super.getPreviousPage();
	}

	protected void createProjectSelectionArea(Composite parent) {
		// Add project selection part common to all pages that extend this class
		GenericCommandWizard commandWizard = getGenericCommandWizard();
		if (commandWizard != null) {

			Collection<IProject> projects = commandWizard.getProjectList();

			if (projects != null && projects.size() > 0) {
				IProjectSelectionHandler handler = new IProjectSelectionHandler() {

					public void handleProjectSelectionChange(IProject project) {
						setProjectInWizard(project);
					}

				};

				projectSelector = new ProjectSelectorFactory(getShell(),
						parent, projects, handler).getProjectSelector();

				if (projectSelector != null) {
					if (projectSelector instanceof ProjectSelectionPart) {
						((ProjectSelectionPart) projectSelector)
								.showProjectSwitchDialogue(false);
					}
					projectSelector.createProjectArea();
					// Set initial project selection, if one doesn't already
					// exist
					if (commandWizard.getSelectedProject() == null) {
						IProject currentSelection = projectSelector
								.getSelectedProject();
						setProjectInWizard(currentSelection);
					} else {
						// Since wizard has a project selected, we must ensure that projectSelector is in agreement with wizard!
						IProject wizardProject = commandWizard.getSelectedProject();
						projectSelector.setProject(wizardProject);
					}
				}
			}
		}
	}

	abstract protected Composite createPageArea(Composite parent);

}
