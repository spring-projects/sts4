/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.maven.MavenCore;

/**
 * Wrapper for Maven Core project
 * 
 * @author Alex Boyko
 *
 */
public class MavenJavaProject implements IJavaProject {

	private MavenProjectClasspath classpath;

	public MavenJavaProject(MavenCore maven, File pom) {
		this.classpath = new MavenProjectClasspath(maven, pom);
	}
		
	@Override
	public MavenProjectClasspath getClasspath() {
		return classpath;
	}
	
	void update(MavenCore maven, File pom) {
		this.classpath = new MavenProjectClasspath(maven, pom);
	}

}
