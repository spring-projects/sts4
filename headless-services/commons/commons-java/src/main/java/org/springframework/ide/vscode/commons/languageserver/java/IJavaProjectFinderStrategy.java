/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;

import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * Strategy foe finding Java project for a document
 *
 * @author Alex Boyko
 */
public interface IJavaProjectFinderStrategy {

	IJavaProject find(File file) throws Exception;
	boolean isProjectRoot(File file);

}
