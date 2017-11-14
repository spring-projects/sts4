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
package org.springframework.ide.vscode.commons.maven.java.classpathfile;

import java.io.File;

import org.springframework.ide.vscode.commons.languageserver.java.AbstractFileToProjectCache;
import org.springframework.ide.vscode.commons.util.FileObserver;

public class JavaProjectWithClasspathFileCache extends AbstractFileToProjectCache<JavaProjectWithClasspathFile> {

	public JavaProjectWithClasspathFileCache(FileObserver fileObserver) {
		super(fileObserver, false, null);
	}

	@Override
	protected boolean update(JavaProjectWithClasspathFile project) {
		 project.update();
		 return true;
	}

	@Override
	protected JavaProjectWithClasspathFile createProject(File cpFile) throws Exception {
		return new JavaProjectWithClasspathFile(cpFile);
	}
	


}
