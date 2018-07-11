/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;

import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IJavaProject;

import com.google.common.collect.ImmutableList;

public interface RunningAppProvider {

	public static final RunningAppProvider NULL = (project) -> ImmutableList.of();

	/**
	 * returns all running spring boot applications on the local machine and if a project
	 * gets passed to the method invocation, strict project matching is taken into account if enabled
	 *
	 * @param project If set (and strict project macthing is enabled), only those running boot apps that can be matched to the project are returned)
	 * @throws Exception
	 */
	Collection<SpringBootApp> getAllRunningSpringApps(IJavaProject project) throws Exception;
}
