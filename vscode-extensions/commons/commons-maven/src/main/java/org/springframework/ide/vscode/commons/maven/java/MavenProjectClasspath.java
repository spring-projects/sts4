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
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.parser.JarSourcesJavadocProvider;
import org.springframework.ide.vscode.commons.java.parser.SourceFolderJavadocProvider;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenException;
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
			return new JandexIndex(classpathEntries, jarFile -> findIndexFile(jarFile), maven.getJavaIndexForJreLibs(), classpathResource -> createJavadocProvider(classpathResource));
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
		return new File(maven.getIndexFolder().toString(), jarFile.getName() + "-" + jarFile.lastModified() + ".jdx");
	}
	
	private Optional<Artifact> getArtifactFromJarFile(File file) throws MavenException {
		return maven.resolveDependencies(project, null).stream().filter(a -> file.equals(a.getFile())).findFirst();
	}
	
	private IJavadocProvider createJavadocProvider(File classpathResource) {
		System.out.println("--------> creating javadoc provider for " + classpathResource);
		if (classpathResource.isDirectory()) {
			if (classpathResource.toString().startsWith(project.getBuild().getOutputDirectory())) {
				return new SourceFolderJavadocProvider(new File(project.getBuild().getSourceDirectory()));
			} else if (classpathResource.toString().startsWith(project.getBuild().getTestOutputDirectory())) {
				return new SourceFolderJavadocProvider(new File(project.getBuild().getTestSourceDirectory()));
			} else {
				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
			}
		} else {
			// Assume it's a JAR file
			return new JarSourcesJavadocProvider(Suppliers.memoize(() -> {
				try {
					Artifact artifact = getArtifactFromJarFile(classpathResource).get();
					return maven.getSources(artifact).getFile().toURI().toURL();
				} catch (MavenException e) {
					Log.log("Failed to find sources JAR for " + classpathResource, e);
				} catch (MalformedURLException e) {
					Log.log("Invalid URL for sources JAR for " + classpathResource, e);
				}
				return null;
			}));
		}
	}

	@Override
	public Stream<String> getClasspathResources() {
		return project.getBuild().getResources().stream().flatMap(resource -> {			
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(resource.getDirectory());
			if (resource.getIncludes() != null && !resource.getIncludes().isEmpty()) {
				scanner.setIncludes(resource.getIncludes().toArray(new String[resource.getIncludes().size()]));
			}
			if (resource.getExcludes() != null && !resource.getExcludes().isEmpty()) {
				scanner.setExcludes(resource.getExcludes().toArray(new String[resource.getExcludes().size()]));
			}
			scanner.setCaseSensitive(false);
			scanner.scan();
			return Arrays.stream(scanner.getIncludedFiles());
		});
	}

}
