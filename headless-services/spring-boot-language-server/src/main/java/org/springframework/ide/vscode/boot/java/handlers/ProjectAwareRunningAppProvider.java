/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
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

public interface ProjectAwareRunningAppProvider {

	static final ProjectAwareRunningAppProvider NULL = (project) -> ImmutableList.of();

	Collection<SpringBootApp> getAllRunningSpringApps(IJavaProject javaProject) throws Exception;
}
