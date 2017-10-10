/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;

/**
 * Implementation of Gradle Java project
 * 
 * @author Alex Boyko
 *
 */
public class GradleJavaProject implements IJavaProject {
	
	private GradleProjectClasspath classpath;
	
	public GradleJavaProject(GradleCore gradle, File projectDir) throws GradleException {
		this.classpath = new GradleProjectClasspath(gradle, projectDir);
	}

	@Override
	public GradleProjectClasspath getClasspath() {
		return classpath;
	}
	
	void update(GradleCore gradle, File projectDir) {
		try {
			this.classpath = new GradleProjectClasspath(gradle, projectDir);
		} catch (GradleException e) {
			Log.log(e);
		}
	}

}
