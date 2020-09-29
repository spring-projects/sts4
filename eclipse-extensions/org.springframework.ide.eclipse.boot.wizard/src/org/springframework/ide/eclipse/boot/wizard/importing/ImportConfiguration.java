/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.importing;

import org.springframework.ide.eclipse.boot.wizard.content.CodeSet;

/**
 * An object that contains the required data to import getting started contents
 * into the workspace using a particular build system.
 */
public interface ImportConfiguration {

	/**
	 * Location of where the project root should be placed or created in the file system.
	 */
	public String getLocation();

	/**
	 * The name of the project in the workspace.
	 */
	public String getProjectName();

	/**
	 * The data used to populate the project.
	 */
	public CodeSet getCodeSet();
	

}
