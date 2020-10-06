/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.docker.runtarget;

import java.io.File;
import java.util.List;

/**
 * Exception raised when BootDash wants to build a docker image but can not
 * find / determine how to build it. I.e. we are looking for a number of
 * different build scripts in project-relative locations. If none of 
 * them are found then this exception is raised.
 */
public class MissingBuildScriptException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public final List<File> locationsChecked;
	
	public MissingBuildScriptException(List<File> locationsChecked) {
		super("Niether maven wraper, gradle wrapper or custom build script found.");
		this.locationsChecked = locationsChecked;
	}

}
