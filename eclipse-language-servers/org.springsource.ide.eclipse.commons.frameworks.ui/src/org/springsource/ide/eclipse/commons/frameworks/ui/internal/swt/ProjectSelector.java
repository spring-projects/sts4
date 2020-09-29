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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Base project selector that creates a label control "Project: " and allows
 * users to add additional controls after the label.
 * @author Nieraj Singh
 */
public class ProjectSelector implements IProjectSelector {

	private Shell shell;
	private Composite parent;
	private String projectName;

	protected ProjectSelector(Shell shell, Composite parent) {
		this.shell = shell;
		this.parent = parent;
	}

	protected Shell getShell() {
		return shell;
	}

	protected void setSelectedProjectName(String projectName) {
		this.projectName = projectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.springsource.sts.frameworks.ui.internal.swt.IProjectSelector#setProject
	 * (org.eclipse.core.resources.IProject)
	 */
	public boolean setProject(IProject project) {
		if (project != null) {
			setSelectedProjectName(project.getName());
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.springsource.sts.frameworks.ui.internal.swt.IProjectSelector#
	 * createProjectArea()
	 */
	public Composite createProjectArea() {

		Composite projectNameArea = new Composite(parent, SWT.NONE);

		// Create a 2 column composite to allow subclasses to add more controls
		// in the same row after the "Project: " label
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false)
				.applyTo(projectNameArea);
		GridDataFactory.fillDefaults().grab(true, false)
				.applyTo(projectNameArea);

		Label projectLabel = new Label(projectNameArea, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
				.grab(false, false).applyTo(projectLabel);
		projectLabel.setText("Project: ");

		return projectNameArea;
	}

	protected String getSelectedProjectName() {
		return projectName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.springsource.sts.frameworks.ui.internal.swt.IProjectSelector#
	 * getSelectedProject()
	 */
	public IProject getSelectedProject() {
		String selectionName = getSelectedProjectName();
		if (selectionName == null) {
			return null;
		}
		return ResourcesPlugin.getWorkspace().getRoot()
				.getProject(selectionName);
	}

}
