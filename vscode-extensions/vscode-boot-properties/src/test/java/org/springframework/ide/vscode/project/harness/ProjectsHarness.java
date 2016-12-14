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
package org.springframework.ide.vscode.project.harness;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.maven.MavenBuilder;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.maven.java.classpathfile.JavaProjectWithClasspathFile;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Test projects harness
 * 
 * @author Alex Boyko
 *
 */
public class ProjectsHarness {
	
	public static final ProjectsHarness INSTANCE = new ProjectsHarness();; 
	
	public Cache<String, IJavaProject> cache = CacheBuilder.newBuilder().concurrencyLevel(1).build();
	
	private enum ProjectType {
		MAVEN,
		CLASSPATH_TXT
	}
	
	private ProjectsHarness() {
	}
	
	public IJavaProject project(ProjectType type, String name) throws Exception {
		return cache.get(type + "/" + name, () -> {
			Path testProjectPath = getProjectPath(name);
			switch (type) {
			case MAVEN:
				return new MavenJavaProject(testProjectPath.resolve(MavenCore.POM_XML).toFile());
			case CLASSPATH_TXT:
				MavenBuilder.newBuilder(testProjectPath).clean().pack().skipTests().execute();
				return new JavaProjectWithClasspathFile(testProjectPath.resolve(MavenCore.CLASSPATH_TXT).toFile());
			default:
				throw new IllegalStateException("Bug!!! Missing case");
			}
		});
	}

	protected Path getProjectPath(String name) throws URISyntaxException, IOException {
//		URI sourceLocation = ProjectsHarness.class.getProtectionDomain().getCodeSource().getLocation().toURI();
//		// file:/Users/aboyko/git/sts4/vscode-extensions/commons/project-test-harness/target/project-test-harness-0.0.1-SNAPSHOT.jar
//		Path testProjectsPath = Paths.get(sourceLocation).getParent().getParent().resolve("test-projects").resolve(name);
//		if (Files.exists(testProjectsPath)) {
//			return testProjectsPath;
//		} else {
//			/*
//			 * If "test-projects" folder is not found then extract test project
//			 * from the jar's "test-projects" folder and copy it in the temp
//			 * folder
//			 */
			return getProjectPathFromClasspath(name);
//		}
	}
	
	private Path getProjectPathFromClasspath(String name) throws URISyntaxException, IOException {
		URI resource = ProjectsHarness.class.getResource("/test-projects/" + name).toURI();
//		if (resource.getScheme().equalsIgnoreCase("jar")) {
//			return getProjectPathFromJar(resource);
//		} else {
			return Paths.get(resource);
//		}
	}
	
//	private Path getProjectPathFromJar(URI jar) throws IOException {
//		final String[] array = jar.toString().split("!");
//		URI firstHalf = URI.create(array[0]);
//		Path tempFolderPath = Paths.get(new File(System.getProperty(MavenCore.JAVA_IO_TMPDIR)).toURI());
//		FileSystem fs =  FileSystems.newFileSystem(firstHalf, Collections.emptyMap());
//		try {
//			Path path = fs.getPath(array[1]);
//			Path projectCopyPath = tempFolderPath.resolve(path.getFileName().toString());
//			if (Files.exists(projectCopyPath)) {
//				recursiveDelete(projectCopyPath);
//			}
//			recursiveCopy(path, tempFolderPath, StandardCopyOption.REPLACE_EXISTING);
//			System.out.println("Copied test project to: " + projectCopyPath);
//			return projectCopyPath;
//		} finally {
//			fs.close();
//		}
//	}
//	
//	private static void recursiveCopy(Path source, Path target, CopyOption... options) throws IOException {
//		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
//			
//			Path destination = target;
//
//			@Override
//			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//				destination = destination.resolve(dir.getFileName().toString());
//				Files.copy(dir, destination, options);
//				return super.preVisitDirectory(dir, attrs);
//			}
//
//			@Override
//			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//				Path newFile = destination.resolve(file.getFileName().toString());
//				Files.copy(file, newFile, options);
//				return super.visitFile(file, attrs);
//			}
//
//			@Override
//			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//				destination = destination.getParent();
//				return super.postVisitDirectory(dir, exc);
//			}
//			
//		});
//	}
//	
//	private static void recursiveDelete(Path path) throws IOException {
//		Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
//			
//			@Override
//			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//				Files.delete(file);
//				return super.visitFile(file, attrs);
//			}
//
//			@Override
//			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//				Files.delete(dir);
//				return super.postVisitDirectory(dir, exc);
//			}
//			
//		});
//	}
	
	public MavenJavaProject mavenProject(String name) throws Exception {
		return (MavenJavaProject) project(ProjectType.MAVEN, name);
	}

	public IJavaProject javaProjectWithClasspathFile(String name) throws Exception {
		return project(ProjectType.CLASSPATH_TXT, name);
	}

}
