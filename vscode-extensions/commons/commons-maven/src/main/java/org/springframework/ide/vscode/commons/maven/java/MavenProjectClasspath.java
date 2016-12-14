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
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject.TypeFilter;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.parser.ParserJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.HtmlJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProviderFromSourceContainer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.MavenException;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * Classpath for a maven project
 * 
 * @author Alex Boyko
 *
 */
public class MavenProjectClasspath implements IClasspath {
	
	public static JavadocProviderTypes providerType = JavadocProviderTypes.HTML;
	
	public enum JavadocProviderTypes {
		JAVA_PARSER,
//		ROASTER,
		HTML
	}
	
	private MavenCore maven;
	private File pom;
	private Supplier<MavenProject> projectSupplier;
	private Supplier<JandexIndex> javaIndex;
	
	public MavenProjectClasspath(File pom) {
		this(pom, MavenCore.getDefault());
	}

	MavenProjectClasspath(File pom, MavenCore maven) {
		this.maven = maven;
		this.pom = pom;
		this.projectSupplier = Suppliers.memoize(() -> {
			try {
				return createMavenProject();
			} catch (MavenException e) {
				Log.log(e);
				return null;
			}
		});
		this.javaIndex = Suppliers.memoize(() -> {
			Stream<Path> classpathEntries = Stream.empty();
			try {
				classpathEntries = getClasspathEntries();
			} catch (Exception e) {
				Log.log(e);
			}
			return new JandexIndex(classpathEntries.map(p -> p.toFile()).collect(Collectors.toList()), jarFile -> findIndexFile(jarFile), classpathResource -> {
				switch (providerType) {
				case JAVA_PARSER:
					return createParserJavadocProvider(classpathResource);
//				case ROASTER:
//					return createRoasterJavadocProvider(classpathResource);
				default:
					return createHtmlJavdocProvider(classpathResource);
				}
			}, maven.getJavaIndexForJreLibs());
		});
	}
	
	private final MavenProject createMavenProject() throws MavenException {
		MavenExecutionResult result = maven.build(pom);
		if (result.hasExceptions()) {
			result.getExceptions().forEach(Log::log);
		}
		return result.getProject();
	}
	
	public boolean exists() {
		return pom.exists();
	}
	
	public String getName() {
		MavenProject project = projectSupplier.get();
		return project == null ? null : project.getName();
	}

	@Override
	public Stream<Path> getClasspathEntries() throws Exception {
//		return Stream.concat(maven.resolveDependencies(project, null).stream().map(artifact -> {
//			return artifact.getFile().toPath();
//		}), projectResolvedOutput());
		return Stream.concat(projectDependencies().stream().map(a -> a.getFile().toPath()), projectOutput().stream().map(f -> f.toPath()));
	}
	
	private Set<Artifact> projectDependencies() {
		MavenProject project = projectSupplier.get();
		return project == null ? Collections.emptySet() : project.getArtifacts();
	}
	
	private List<File> projectOutput() {
		MavenProject project = projectSupplier.get();
		if (project == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(new File(project.getBuild().getOutputDirectory()), new File(project.getBuild().getTestOutputDirectory()));
		}
	}
	
	public String getOutputFolder() {
		MavenProject project = projectSupplier.get();
		return project == null ? null : project.getBuild().getOutputDirectory();
	}
	
	public IType findType(String fqName) {
		return javaIndex.get().findType(fqName);
	}
	
	public Flux<Tuple2<IType, Double>> fuzzySearchType(String searchTerm, TypeFilter typeFilter) {
		return javaIndex.get().fuzzySearchTypes(searchTerm, typeFilter);
	}
	
	public Flux<Tuple2<String, Double>> fuzzySearchPackages(String searchTerm) {
		return javaIndex.get().fuzzySearchPackages(searchTerm);
	}

	public Flux<IType> allSubtypesOf(IType type) {
		return javaIndex.get().allSubtypesOf(type);
	}

	private File findIndexFile(File jarFile) {
		return new File(maven.getIndexFolder().toString(), jarFile.getName() + "-" + jarFile.lastModified() + ".jdx");
	}
	
	private Optional<Artifact> getArtifactFromJarFile(File file) throws MavenException {
		MavenProject project = projectSupplier.get();
		return project.getArtifacts().stream().filter(a -> file.equals(a.getFile())).findFirst();
	}
	
	@Override
	public Stream<String> getClasspathResources() {
		MavenProject project = projectSupplier.get();
		if (project == null) {
			return Stream.empty();
		}
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

	private IJavadocProvider createParserJavadocProvider(File classpathResource) {
		MavenProject project = projectSupplier.get();
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
	
	private IJavadocProvider createHtmlJavdocProvider(File classpathResource) {
		MavenProject project = projectSupplier.get();
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

}
