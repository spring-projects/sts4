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

import java.nio.file.Path;

import org.springframework.ide.vscode.commons.maven.java.classpathfile.JavaProjectWithClasspathFile;

/**
 * Maven projects methods
 * 
 * @author Alex Boyko
 *
 */
public class Projects {

	public static final String CLASSPATH_TXT = "classpath.txt";
	public static final String POM_XML = "pom.xml";

	public static MavenJavaProject createMavenJavaProject(Path projectPath) throws Exception {
		return new MavenJavaProject(projectPath.resolve(Projects.POM_XML).toFile());
	}

	public static JavaProjectWithClasspathFile createJavaProjectWithClasspathFile(Path projectPath) throws Exception {
		return new JavaProjectWithClasspathFile(projectPath.resolve(Projects.CLASSPATH_TXT).toFile());
	}

}
