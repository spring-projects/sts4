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
package org.springframework.ide.vscode.commons.maven.java.classpathfile;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.maven.MavenCore;

/**
 * Classpath for a project containing classpath text file
 * 
 * @author Alex Boyko
 *
 */
public class FileClasspath implements IClasspath {

	private Path classpathFilePath;

	public FileClasspath(Path classpathFilePath) {
		this.classpathFilePath = classpathFilePath;
	}

	@Override
	public Stream<Path> getClasspathEntries() throws Exception {
		return Stream.concat(MavenCore.readClassPathFile(classpathFilePath),
				Stream.of(classpathFilePath.getParent().resolve("target/classes"),
						classpathFilePath.getParent().resolve("target/test-classes")));
	}

	@Override
	public Stream<String> getClasspathResources() {
		return Stream.empty();
	}

}
