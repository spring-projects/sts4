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
package org.springsource.ide.eclipse.commons.frameworks.ui.internal.parameters.editors;

import org.eclipse.core.resources.IProject;

/**
 * Listeners implementing this interface get notified when a project selection has changed. Usually implemented
 * by UI components that need to be notified when a project selection has changed.
 * @author Nieraj Singh
 */
public interface IProjectSelectionChangeListener {
	
	public void projectSelectionChanged(IProject project);

}
