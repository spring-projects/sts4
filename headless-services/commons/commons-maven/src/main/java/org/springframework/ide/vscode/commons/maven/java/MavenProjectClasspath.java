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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.parser.ParserJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.HtmlJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProviderFromSourceContainer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenException;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

/**
 * Classpath for a maven project
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectClasspath extends JandexClasspath {
	
	private MavenCore maven;
	private File pom;
	private MavenProject project;
	
	MavenProjectClasspath(MavenCore maven, File pom) throws MavenException {
		super();
		this.maven = maven;
		this.pom = pom;
		this.project = createMavenProject();
	}
	
	@Override
	protected JandexIndex[] getBaseIndices() {
		return new JandexIndex[] { maven.getJavaIndexForJreLibs() };
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
	
	public String getName() {
		return project == null ? null : project.getArtifact().getArtifactId();
	}

	@Override
	public ImmutableList<Path> getClasspathEntries() throws Exception {
//		return Stream.concat(maven.resolveDependencies(project, null).stream().map(artifact -> {
//			return artifact.getFile().toPath();
//		}), projectResolvedOutput());
		ImmutableList<Path> classpathEntries = ImmutableList.copyOf(Stream.concat(projectDependencies().stream().map(a -> a.getFile().toPath()),
				projectOutput().stream().map(f -> f.toPath())).collect(Collectors.toList()));
		return classpathEntries;
	}
	
	private Set<Artifact> projectDependencies() {
		return project == null ? Collections.emptySet() : project.getArtifacts();
	}
	
	private List<File> projectOutput() {
		if (project == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()));
		}
	}
	
	public Path getOutputFolder() {
		return project == null ? null : new File(project.getBuild().getOutputDirectory()).toPath();
	}
	
	private Optional<Artifact> getArtifactFromJarFile(File file) throws MavenException {
		return project.getArtifacts().stream().filter(a -> file.equals(a.getFile())).findFirst();
	}
	
	@Override
	public ImmutableList<String> getClasspathResources() {
		if (project == null) {
			return ImmutableList.of();
		}
		return ImmutableList.copyOf(project.getBuild().getResources().stream().filter(resource -> new File(resource.getDirectory()).exists()).flatMap(resource -> {
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
		}).toArray(String[]::new));
	}

	/*
	 * Roaster lib is experiment to generate javadoc from source. Commented out for now since roaster lib is taken out
	 */
//	private IJavadocProvider createRoasterJavadocProvider(File classpathResource) {
//		if (classpathResource.isDirectory()) {
//			if (classpathResource.toString().startsWith(project.getBuild().getOutputDirectory())) {
//				return new RoasterJavadocProvider(type -> {
//					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
//							.sourceUrl(new File(project.getBuild().getSourceDirectory()).toURI().toURL(), type);
//				});
//			} else if (classpathResource.toString().startsWith(project.getBuild().getTestOutputDirectory())) {
//				return new RoasterJavadocProvider(type -> {
//					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
//							.sourceUrl(new File(project.getBuild().getTestSourceDirectory()).toURI().toURL(), type);
//				});
//			} else {
//				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
//			}
//		} else {
//			// Assume it's a JAR file
//			return new RoasterJavadocProvider(type -> {
//				try {
//					Artifact artifact = getArtifactFromJarFile(classpathResource).get();
//					URL sourceContainer = maven.getSources(artifact).getFile().toURI().toURL();
//					return SourceUrlProviderFromSourceContainer.JAR_SOURCE_URL_PROVIDER.sourceUrl(sourceContainer,
//							type);
//				} catch (MavenException e) {
//					Log.log("Failed to find sources JAR for " + classpathResource, e);
//				} catch (MalformedURLException e) {
//					Log.log("Invalid URL for sources JAR for " + classpathResource, e);
//				}
//				return null;
//			});
//		}
//	}

	protected IJavadocProvider createParserJavadocProvider(File classpathResource) {
		if (project == null) {
			return null;
		}
		if (classpathResource.isDirectory()) {
			if (classpathResource.toString().startsWith(project.getBuild().getOutputDirectory())) {
				return new ParserJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getBuild().getSourceDirectory()).toURI().toURL(), type);
				});
			} else if (classpathResource.toString().startsWith(project.getBuild().getTestOutputDirectory())) {
				return new ParserJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getBuild().getTestSourceDirectory()).toURI().toURL(), type);
				});
			} else {
				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
			}
		} else {
			// Assume it's a JAR file
			return new ParserJavadocProvider(type -> {
				try {
					Artifact artifact = getArtifactFromJarFile(classpathResource).get();
					URL sourceContainer = maven.getSources(artifact, project.getRemoteArtifactRepositories()).getFile().toURI().toURL();
					return SourceUrlProviderFromSourceContainer.JAR_SOURCE_URL_PROVIDER.sourceUrl(sourceContainer,
							type);
				} catch (MavenException e) {
					Log.log("Failed to find sources JAR for " + classpathResource, e);
				} catch (MalformedURLException e) {
					Log.log("Invalid URL for sources JAR for " + classpathResource, e);
				}
				return null;
			});
		}
	}
	
	protected IJavadocProvider createHtmlJavdocProvider(File classpathResource) {
		if (project == null) {
			return null;
		}
		if (classpathResource.isDirectory()) {
			if (classpathResource.toString().startsWith(project.getBuild().getOutputDirectory())) {
				return new HtmlJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.JAVADOC_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getModel().getReporting().getOutputDirectory(), "apidocs").toURI().toURL(), type);
				});
			} else if (classpathResource.toString().startsWith(project.getBuild().getTestOutputDirectory())) {
				return new ParserJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.JAVADOC_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getModel().getReporting().getOutputDirectory(), "apidocs").toURI().toURL(), type);
				});
			} else {
				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
			}
		} else {
			// Assume it's a JAR file
			return new HtmlJavadocProvider(type -> {
				try {
					Artifact artifact = getArtifactFromJarFile(classpathResource).get();
					URL sourceContainer = maven.getJavadoc(artifact, project.getRemoteArtifactRepositories()).getFile().toURI().toURL();
					return SourceUrlProviderFromSourceContainer.JAR_JAVADOC_URL_PROVIDER.sourceUrl(sourceContainer,
							type);
				} catch (MavenException e) {
					Log.log("Failed to find sources JAR for " + classpathResource, e);
				} catch (MalformedURLException e) {
					Log.log("Invalid URL for sources JAR for " + classpathResource, e);
				}
				return null;
			});
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof MavenProjectClasspath) {
			MavenProjectClasspath other = (MavenProjectClasspath) obj;
			try {
				if (pom.equals(other.pom)
						&& Objects.equal(project, other.project)) {
					return super.equals(obj);
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		}
		return false;
	}

	@Override
	public ImmutableList<String> getSourceFolders() {
		return ImmutableList.of(project.getBuild().getSourceDirectory());
	}

}
