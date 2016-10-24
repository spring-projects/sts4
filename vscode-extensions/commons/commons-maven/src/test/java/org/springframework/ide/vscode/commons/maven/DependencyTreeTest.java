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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.project.MavenProject;
import org.junit.Test;
import org.springframework.ide.vscode.commons.util.ExternalCommand;
import org.springframework.ide.vscode.commons.util.ExternalProcess;

/**
 * Tests for comparing maven calculated dependencies with ours
 * 
 * @author Alex Boyko
 *
 */
public class DependencyTreeTest {
	
	/**
	 * Build test project if it's not built already
	 * @throws Exception
	 */
	private static void buildProject(Path testProjectPath) throws Exception {
		if (!Files.exists(testProjectPath.resolve("classpath.txt"))) {
			Path mvnwPath = System.getProperty("os.name").toLowerCase().startsWith("win")
					? testProjectPath.resolve("mvnw.cmd") : testProjectPath.resolve("mvnw");
			mvnwPath.toFile().setExecutable(true);
			ExternalProcess process = new ExternalProcess(testProjectPath.toFile(),
					new ExternalCommand(mvnwPath.toAbsolutePath().toString(), "clean", "package"), true);
			if (process.getExitValue() != 0) {
				throw new RuntimeException("Failed to build test project");
			}
		}
	}
	
	private static Set<Path> readClassPathFile(Path classPathFilePath) throws IOException {
		InputStream in = Files.newInputStream(classPathFilePath);
		String text = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining());
		Path dir = classPathFilePath.getParent();
		return Arrays.stream(text.split(File.pathSeparator)).map(dir::resolve).collect(Collectors.toSet());
	}
	
	@Test
	public void mavenTest() throws Exception {
		Path testProjectPath = Paths.get(DependencyTreeTest.class.getResource("/demo-1").toURI());
		buildProject(testProjectPath);
		
		MavenProject project = MavenCore.getInstance().readProject(testProjectPath.resolve("pom.xml").toFile());
		Set<Path> calculatedClassPath = MavenCore.getInstance().resolveDependencies(project, null).stream().map(artifact -> {
			return Paths.get(artifact.getFile().toURI());
		}).collect(Collectors.toSet());;
		
		Set<Path> expectedClasspath = readClassPathFile(testProjectPath.resolve("classpath.txt"));
		assertEquals(expectedClasspath, calculatedClassPath);		
	}

}
