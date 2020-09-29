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
import org.eclipse.swt.widgets.Composite;

/**
 * Implementors can contribute project selection functionality and UI to
 * component that requires project selection.
 * @author Nieraj Singh
 */
public interface IProjectSelector {

	/**
	 * 
	 * @return currently selected project, or null if nothing selected or
	 *        available for selection
	 */
	public IProject getSelectedProject();

	/**
	 * Sets a project in the current project list of the selector. Nothing may
	 * happen if the project is not currently present in the list.
	 * 
	 * @param project
	 *           to set in a list of projects.
	 * @return true if project exists in list and successfully selected. False
	 *        otherwise
	 */
	public boolean setProject(IProject project);

	/**
	 * Project area containing UI for project selection
	 * @return Composite for area that must not be null
	 */
	public Composite createProjectArea();

}
