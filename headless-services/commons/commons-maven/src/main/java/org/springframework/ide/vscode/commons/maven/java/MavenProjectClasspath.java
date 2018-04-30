/*******************************************************************************
 * Copyright (c) 2016, 2018 Pivotal, Inc.
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
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Generated;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.HtmlJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.TypeUrlProviderFromContainerUrl;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenException;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.RunnableWithException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.gson.internal.Streams;

/**
 * Classpath for a maven project
 *
 * @author Alex Boyko
 *
 */
public class MavenProjectClasspath implements IClasspath {

	private MavenCore maven;
	private File pom;
	private ClasspathData cachedData;

	MavenProjectClasspath(MavenCore maven, File pom) throws Exception {
		super();
		this.maven = maven;
		this.pom = pom;
		this.cachedData = createClasspathData();
	}

	private final MavenProject createMavenProject() throws MavenException {
		try {
			// Read with resolved dependencies
			return maven.readProject(pom, true);
		} catch (MavenException e) {
			Log.log(e);
			return maven.readProject(pom, false);
		}
	}

	public File getPomFile() {
		return pom;
	}

	MavenCore maven() {
		return maven;
	}

	public boolean exists() {
		return pom.exists();
	}

	@Override
	public String getName() {
		return cachedData != null ? cachedData.getName() : null;
	}

	private ImmutableList<CPE> resolveClasspathEntries(MavenProject project) throws Exception {
		LinkedHashSet<CPE> entries = new LinkedHashSet<>();
		safe(() -> maven.getJreLibs().forEach(path -> safe(() -> {
			CPE cpe = CPE.binary(path.toString());
			String javaVersion = maven.getJavaRuntimeMinorVersion();
			if (javaVersion == null) {
				javaVersion = "8";
			}
			cpe.setJavadocContainerUrl(new URL("https://docs.oracle.com/javase/" + javaVersion + "/docs/api/"));
			cpe.setSystem(true);
			entries.add(cpe);
		})));
		//Add jar dependencies...
		for (Artifact a : projectDependencies(project)) {
			File f = a.getFile();
			if (f!=null) {
				CPE cpe = CPE.binary(a.getFile().toPath().toString());
				safe(() -> { //add javadoc
					Artifact jdoc = maven.getJavadoc(a, project.getRemoteArtifactRepositories());
					if (jdoc!=null) {
						cpe.setJavadocContainerUrl(jdoc.getFile().toURI().toURL());
					}
				});
				safe(() -> { //add source
					Artifact source = maven.getSources(a, project.getRemoteArtifactRepositories());
					if (source!=null) {
						cpe.setSourceContainerUrl(source.getFile().toURI().toURL());
					}
				});
				entries.add(cpe);
			}
		}
		//Add source folders...
		{	//main/java
			File sourceFolder = new File(project.getBuild().getSourceDirectory());
			File outputFolder = new File(project.getBuild().getOutputDirectory());
			CPE cpe = CPE.source(sourceFolder, outputFolder);
			safe(() -> {
				String reportingDir = project.getModel().getReporting().getOutputDirectory();
				if (reportingDir!=null) {
					File apidocs = new File(new File(reportingDir), "apidocs");
					cpe.setJavadocContainerUrl(apidocs.toURI().toURL());
				}
			});
			entries.add(cpe);
		}
		{	//main/resources
			for (Resource resource : project.getBuild().getResources()) {
				File sourceFolder = new File(resource.getDirectory());
				String targetPath = resource.getTargetPath();
				if (targetPath==null) {
					targetPath = project.getBuild().getOutputDirectory();
				}
				entries.add(CPE.source(sourceFolder, new File(targetPath)));
			}
		}
		{	//test/resources
			for (Resource resource : project.getBuild().getTestResources()) {
				File sourceFolder = new File(resource.getDirectory());
				String targetPath = resource.getTargetPath();
				if (targetPath==null) {
					targetPath = project.getBuild().getTestOutputDirectory();
				}
				entries.add(CPE.source(sourceFolder, targetPath==null ? null : new File(targetPath)));
			}
		}
		{	//test/java
			File sourceFolder = new File(project.getBuild().getTestSourceDirectory());
			File outputFolder = new File(project.getBuild().getTestOutputDirectory());
			CPE cpe = CPE.source(sourceFolder, outputFolder);
			safe(() -> {
				String reportingDir = project.getModel().getReporting().getOutputDirectory();
				if (reportingDir!=null) {
					File apidocs = new File(new File(reportingDir), "apidocs");
					cpe.setJavadocContainerUrl(apidocs.toURI().toURL());
				}
			});
			entries.add(cpe);
		}
		return ImmutableList.copyOf(entries);
	}

	@Override
	public ImmutableList<CPE> getClasspathEntries() throws Exception {
		return cachedData != null ? ImmutableList.copyOf(cachedData.getClasspathEntries()) : ImmutableList.of();
	}

	private Set<Artifact> projectDependencies(MavenProject project) {
		return project == null ? Collections.emptySet() : project.getArtifacts();
	}

	private List<File> projectOutput(MavenProject project) {
		if (project == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()));
		}
	}

	private static void safe(RunnableWithException do_stuff) {
		try {
			do_stuff.run();
		} catch (Exception e) {
// 			log.error("", e);
		}
	}

	private ClasspathData createClasspathData() throws Exception {
		MavenProject project = createMavenProject();

		ImmutableList<CPE> entries = resolveClasspathEntries(project);
		String name = project.getArtifact().getArtifactId();

		return new ClasspathData(name, new LinkedHashSet<>(entries));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MavenProjectClasspath) {
			MavenProjectClasspath other = (MavenProjectClasspath) obj;
			try {
				if (pom.equals(other.pom)
						&& Objects.equal(cachedData, other.cachedData)) {
					return super.equals(obj);
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		}
		return false;
	}

}
