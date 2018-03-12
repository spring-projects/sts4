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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.ClasspathData;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
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
	private MavenClasspathData cachedData;
	
	MavenProjectClasspath(MavenCore maven, File pom) throws Exception {
		super();
		this.maven = maven;
		this.pom = pom;
		this.cachedData = createClasspathData();
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
		return cachedData != null ? cachedData.name : null;
	}
	
	private ImmutableList<Path> resolveClasspathEntries(MavenProject project) throws Exception {
//		return Stream.concat(maven.resolveDependencies(project, null).stream().map(artifact -> {
//			return artifact.getFile().toPath();
//		}), projectResolvedOutput());
		ImmutableList<Path> classpathEntries = ImmutableList.copyOf(Stream.concat(projectDependencies(project).stream().map(a -> a.getFile().toPath()),
				projectOutput(project).stream().map(f -> f.toPath())).collect(Collectors.toList()));
		return classpathEntries;
	}

	@Override
	public ImmutableList<Path> getClasspathEntries() throws Exception {
		return cachedData != null ? ImmutableList.copyOf(cachedData.classpathEntries) : ImmutableList.of();
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
	
	private Path resolveOutputFolder(MavenProject project) {
		return project == null ? null : new File(project.getBuild().getOutputDirectory()).toPath();
	}
	
	public Path getOutputFolder() {
		return cachedData != null ? cachedData.outputFolder : null;
	}
	
	private ImmutableList<String> resolveClasspathResources(MavenProject project) {
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
	
	@Override
	public ImmutableList<String> getClasspathResources() {
		return cachedData != null ? ImmutableList.copyOf(cachedData.classpathResources) : ImmutableList.of();
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
	

	@Override
	public Optional<URL> sourceContainer(File classpathResource) {
		if (cachedData == null) {
			return Optional.empty();
		}
		return cachedData.artifacts.stream().filter(a -> classpathResource.equals(a.getFile())).findFirst().map(artifact -> {
				try {
					return maven.getSources(artifact, cachedData.remoteArtifactRepositories).getFile().toURI().toURL();
				} catch (MalformedURLException e) {
					Log.log("Invalid URL for sources JAR for " + classpathResource, e);
					return null;
				} catch (MavenException e) {
					Log.log("Failed to find sources JAR for " + classpathResource, e);
					return null;
				}
		});
	}

	@Override
	protected IJavadocProvider createHtmlJavdocProvider(File classpathResource) {
		if (cachedData == null) {
			return null;
		}
		if (classpathResource.isDirectory()) {
			if (classpathResource.toString().startsWith(cachedData.outputDirectory)) {
				return new HtmlJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.JAVADOC_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(cachedData.reportingOutputDirectory, "apidocs").toURI().toURL(), type.getFullyQualifiedName());
				});
			} else if (classpathResource.toString().startsWith(cachedData.testOutputDirectory)) {
				return new HtmlJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.JAVADOC_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(cachedData.reportingOutputDirectory, "apidocs").toURI().toURL(), type.getFullyQualifiedName());
				});
			} else {
				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
			}
		} else {
			// Assume it's a JAR file
			return new HtmlJavadocProvider(type -> {
				try {
					Artifact artifact = cachedData.artifacts.stream().filter(a -> classpathResource.equals(a.getFile())).findFirst().get();
					URL sourceContainer = maven.getJavadoc(artifact, cachedData.remoteArtifactRepositories).getFile().toURI().toURL();
					return SourceUrlProviderFromSourceContainer.JAR_JAVADOC_URL_PROVIDER.sourceUrl(sourceContainer,
							type.getFullyQualifiedName());
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
						&& Objects.equal(cachedData, other.cachedData)) {
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
		return cachedData != null ? ImmutableList.of(cachedData.sourceDirectory) : ImmutableList.of();
	}

	@Override
	public MavenClasspathData createClasspathData() throws Exception {
		MavenProject project = createMavenProject();

		ImmutableList<Path> entries = resolveClasspathEntries(project);
		String name = project.getArtifact().getArtifactId();
		ImmutableList<String> resources = resolveClasspathResources(project);
		Path outputFolder = resolveOutputFolder(project);
		
		MavenClasspathData data = new MavenClasspathData(name, new LinkedHashSet<>(entries),
				new LinkedHashSet<>(resources), outputFolder);
		
		data.outputDirectory = project.getBuild().getOutputDirectory();
		data.reportingOutputDirectory = project.getModel().getReporting().getOutputDirectory();
		data.testOutputDirectory = project.getBuild().getTestOutputDirectory();
		data.testSourceDirectory = project.getBuild().getTestSourceDirectory();
		data.artifacts = project.getArtifacts();
		data.remoteArtifactRepositories = project.getRemoteArtifactRepositories();
		data.sourceDirectory = project.getBuild().getSourceDirectory();
		return data;
	}
	
	class MavenClasspathData extends ClasspathData {

		private String testSourceDirectory;
		private List<ArtifactRepository> remoteArtifactRepositories;
		private Set<Artifact> artifacts;
		private String testOutputDirectory;
		private String reportingOutputDirectory;
		private String outputDirectory;
		private String sourceDirectory;
		

		public MavenClasspathData(String name, Set<Path> classpathEntries, Set<String> classpathResources,
				Path outputFolder) {
			super(name, classpathEntries, classpathResources, outputFolder);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			MavenClasspathData other = (MavenClasspathData) obj;
			if (artifacts == null) {
				if (other.artifacts != null)
					return false;
			} else if (!artifacts.equals(other.artifacts))
				return false;
			if (outputDirectory == null) {
				if (other.outputDirectory != null)
					return false;
			} else if (!outputDirectory.equals(other.outputDirectory))
				return false;
			if (remoteArtifactRepositories == null) {
				if (other.remoteArtifactRepositories != null)
					return false;
			} else if (!remoteArtifactRepositories.equals(other.remoteArtifactRepositories))
				return false;
			if (reportingOutputDirectory == null) {
				if (other.reportingOutputDirectory != null)
					return false;
			} else if (!reportingOutputDirectory.equals(other.reportingOutputDirectory))
				return false;
			if (sourceDirectory == null) {
				if (other.sourceDirectory != null)
					return false;
			} else if (!sourceDirectory.equals(other.sourceDirectory))
				return false;
			if (testOutputDirectory == null) {
				if (other.testOutputDirectory != null)
					return false;
			} else if (!testOutputDirectory.equals(other.testOutputDirectory))
				return false;
			if (testSourceDirectory == null) {
				if (other.testSourceDirectory != null)
					return false;
			} else if (!testSourceDirectory.equals(other.testSourceDirectory))
				return false;
			return true;
		}

		
	}
}
