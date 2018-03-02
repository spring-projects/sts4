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
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.springframework.ide.vscode.commons.languageserver.ClasspathService;
import org.springframework.ide.vscode.commons.maven.MavenCore;

import com.google.common.collect.ImmutableList;

public class MavenProjectJdtClasspath extends MavenProjectClasspath {
	

	public MavenProjectJdtClasspath(ClasspathService classpathService, MavenCore maven, File pom) throws Exception {
		super(classpathService, maven, pom);
		this.classpathService = classpathService;
	}

	@Override
	protected ImmutableList<Path> resolveClasspathEntries(MavenProject project, String sourceDirectory) throws Exception {
		ImmutableList<Path> classpaths = fromClientClasspathService(project, sourceDirectory);
		if (classpaths == null) {
			classpaths = super.resolveClasspathEntries(project, sourceDirectory);
		}
		return classpaths;
	}

	private ImmutableList<Path> fromClientClasspathService(MavenProject project, String sourceDirectory) {
		if (classpathService != null) {
			Object fromService = classpathService.classpathEvent(project.getName(), sourceDirectory);
			
			if (fromService instanceof Map<?, ?>) {
				Map<?, ?> asMap = (Map<?, ?>) fromService;
				Object valObj = asMap.get("classpath");
				if (valObj instanceof Collection<?>) {
					return ImmutableList.copyOf(((Collection<?>) valObj).stream()
							.map(a -> {
								if (a instanceof String) {
									return new File((String) a).toPath();
								} else {
									return null;
								}})
							.filter((path) -> path != null)
							.collect(Collectors.toList())
					);
				}
			}
		}
		return null;
	}
}
