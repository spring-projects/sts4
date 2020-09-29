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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Specialised class that displays a project name for a single project selection.
 * @author Nieraj Singh
 */
public class ProjectLabel extends ProjectSelector {

	private String projectName;

	public ProjectLabel(Shell shell, Composite parent, String projectName) {
		super(shell, parent);
		this.projectName = projectName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.springsource.sts.frameworks.ui.internal.swt.ProjectSelector#createProjectArea()
	 */
	public Composite createProjectArea() {
		setSelectedProjectName(projectName);

		Composite parent = super.createProjectArea();

		Label projectLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER)
				.grab(false, false).applyTo(projectLabel);
		projectLabel.setText(projectName);

		return parent;
	}

}
