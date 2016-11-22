package org.springframework.ide.vscode.commons.maven;

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
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.junit.Test;

/**
 * Tests for comparing maven calculated dependencies with ours
 * 
 * @author Alex Boyko
 *
 */
public class DependencyTreeTest {
	
	private void testMavenClasspath(String projectName) throws Exception {
		Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/" + projectName).toURI());
		MavenBuilder.newBuilder(testProjectPath).clean().pack().skipTests().execute();

		MavenProject project = MavenCore.getInstance().readProject(testProjectPath.resolve(MavenCore.POM_XML).toFile());
		Set<Path> calculatedClassPath = MavenCore.getInstance().resolveDependencies(project, null).stream().map(artifact -> {
			return Paths.get(artifact.getFile().toURI());
		}).collect(Collectors.toSet());;
		
		Set<Path> expectedClasspath = MavenCore.readClassPathFile(testProjectPath.resolve(MavenCore.CLASSPATH_TXT)).collect(Collectors.toSet());
		assertEquals(expectedClasspath, calculatedClassPath);		
	}
	
	@Test
	public void mavenClasspathTest_1() throws Exception {
		testMavenClasspath("empty-boot-project-with-classpath-file");
	}

	@Test
	public void mavenClasspathTest_2() throws Exception {
		testMavenClasspath("gs-rest-service-cors-boot-1.4.1-with-classpath-file");
	}
	
}
