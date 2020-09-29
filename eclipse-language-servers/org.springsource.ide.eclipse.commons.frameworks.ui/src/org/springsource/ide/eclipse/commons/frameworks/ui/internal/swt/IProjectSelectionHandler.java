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

/**
 * Handler for project selection changes, where the newly selected project is
 * passed as an argument. This handler is usually invoked by UI components that
 * display a list of projects.
 * @author Nieraj Singh
 */
public interface IProjectSelectionHandler {

	/**
	 * Handles a project selection change.
	 * 
	 * @param project
	 *           newly selected project
	 */
	public void handleProjectSelectionChange(IProject project);

}
