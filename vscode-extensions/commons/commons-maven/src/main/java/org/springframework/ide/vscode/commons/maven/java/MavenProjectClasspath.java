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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.maven.project.MavenProject;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Classpath for a maven project
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectClasspath implements IClasspath {

	private MavenCore maven;
	private MavenProject project;
	private Supplier<JandexIndex> javaIndex;

	public MavenProjectClasspath(MavenProject project) {
		this(project, MavenCore.getInstance());
	}

	MavenProjectClasspath(MavenProject project, MavenCore maven) {
		this.maven = maven;
		this.project = project;
		this.javaIndex = Suppliers.memoize(() -> {
			Stream<Path> classpathEntries = Stream.empty();
			try {
				classpathEntries = getClasspathEntries();
			} catch (Exception e) {
				Log.log(e);
			}
			return new JandexIndex(classpathEntries, jarFile -> findIndexFile(jarFile), maven.getJavaIndexForJreLibs());
		});
	}

	@Override
	public Stream<Path> getClasspathEntries() throws Exception {
		return Stream.concat(maven.resolveDependencies(project, null).stream().map(artifact -> {
			return Paths.get(artifact.getFile().toURI());
		}), Stream.of(Paths.get(new File(project.getBuild().getOutputDirectory()).toURI()),
				Paths.get(new File(project.getBuild().getTestOutputDirectory()).toURI())));
	}
	
	public IType findType(String fqName) {
		return javaIndex.get().findType(fqName);
	}
	
	private File findIndexFile(File jarFile) {
		return new File(System.getProperty(MavenCore.JAVA_IO_TMPDIR), jarFile.getName() + jarFile.lastModified());
	}
	
}
