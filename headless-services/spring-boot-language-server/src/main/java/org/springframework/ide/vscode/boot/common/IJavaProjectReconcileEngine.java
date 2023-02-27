/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.common;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;

public interface IJavaProjectReconcileEngine {

	void reconcile(IJavaProject project, ProgressService progressService);
	
	void clear(IJavaProject project);
	
}
