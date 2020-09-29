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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Based on the number of projects in a project list, this factory creates a
 * project selector with the correct UI. For example, a project list with one
 * entry will create a project selector with a label displaying the single
 * project name, whereas a project list with multiple entries may return a
 * project selector with a combo control or table. Null is returned for project
 * lists that are null or empty.
 * @author Nieraj Singh
 */
public class ProjectSelectorFactory {

	private Shell shell;
	private Composite parent;
	private Collection<IProject> projects;
	private IProjectSelectionHandler handler;

	/**
	 * 
	 * @param shell
	 *           for the project selector, in case the project selector has
	 *           dialogues
	 * @param parent
	 *           composite for the project selector area containing project
	 *           selection UI
	 * @param projects
	 *           list of projects to display in the project selector
	 * @param handler
	 *           optional handler that gets invoked when a project selection
	 *           change occurs
	 */
	public ProjectSelectorFactory(Shell shell, Composite parent,
			Collection<IProject> projects, IProjectSelectionHandler handler) {
		this.shell = shell;
		this.parent = parent;
		this.projects = projects;
		this.handler = handler;
	}

	/**
	 * Based on the number of entries in a project list, create a corresponding
	 * project selector. If project list is null or empty, null is returned. A
	 * new instance of a project selector is created every time this method is
	 * invoked.
	 * 
	 * @return new instance of a project selector, or null if project list is
	 *        null or empty
	 */
	public IProjectSelector getProjectSelector() {
		if (canCreate()) {
			if (projects.size() == 1) {
				return new ProjectLabel(shell, parent, projects.iterator()
						.next().getName());
			} else {
				return new ProjectSelectionPart(shell, parent, projects,
						handler);
			}
		}
		return null;
	}

	protected boolean canCreate() {
		return projects != null && !projects.isEmpty() && shell != null
				&& !shell.isDisposed() && parent != null;
	}

}
