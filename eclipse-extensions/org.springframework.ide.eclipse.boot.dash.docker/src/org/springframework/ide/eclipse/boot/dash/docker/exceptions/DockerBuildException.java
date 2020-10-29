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
package org.springframework.ide.eclipse.boot.dash.docker.exceptions;

import org.springframework.ide.eclipse.boot.dash.api.AppConsole;

public class DockerBuildException extends Exception {

	private static final long serialVersionUID = 1L;

	public DockerBuildException(String string) {
		super(string);
	}

	public void writeDetailedExplanation(AppConsole console) throws Exception {
	}

}
