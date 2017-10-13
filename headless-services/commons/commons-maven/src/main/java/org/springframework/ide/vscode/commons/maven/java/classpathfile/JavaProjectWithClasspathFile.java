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
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * Java project that contains classpath text file 
 * 
 * @author Alex Boyko
 *
 */
public class JavaProjectWithClasspathFile implements IJavaProject {

	private File cpFile;
	private FileClasspath classpath;

	public JavaProjectWithClasspathFile(File cpFile) {
		this.cpFile = cpFile;
		this.classpath = new FileClasspath(Paths.get(cpFile.toURI()));
	}

	@Override
	public IClasspath getClasspath() {
		return classpath;
	}

	@Override
	public String toString() {
		return "JavaProjectWithClasspathFile("+cpFile+")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cpFile == null) ? 0 : cpFile.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaProjectWithClasspathFile other = (JavaProjectWithClasspathFile) obj;
		if (cpFile == null) {
			if (other.cpFile != null)
				return false;
		} else if (!cpFile.equals(other.cpFile))
			return false;
		return true;
	}
	
	void update() {
		this.classpath = new FileClasspath(Paths.get(cpFile.toURI()));
	}

}