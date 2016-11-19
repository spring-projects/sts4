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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.java.parser.ParserJavadocProvider;
import org.springframework.ide.vscode.commons.java.roaster.RoasterJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.HtmlJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProviderFromSourceContainer;
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
	
	public static JavadocProviderTypes providerType = JavadocProviderTypes.JAVA_PARSER;
	
	public enum JavadocProviderTypes {
		JAVA_PARSER,
		ROASTER,
		HTML
	}
	
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
			return new JandexIndex(classpathEntries, jarFile -> findIndexFile(jarFile), classpathResource -> {
				switch (providerType) {
				case JAVA_PARSER:
					return createParserJavadocProvider(classpathResource);
				case ROASTER:
					return createRoasterJavadocProvider(classpathResource);
				default:
					return createHtmlJavdocProvider(classpathResource);
				}
			}, maven.getJavaIndexForJreLibs());
		});
	}

	@Override
	public Stream<Path> getClasspathEntries() throws Exception {
		return Stream.concat(maven.resolveDependencies(project, null).stream().map(artifact -> {
			return artifact.getFile().toPath();
		}), Stream.of(new File(project.getBuild().getOutputDirectory()).toPath(),
				new File(project.getBuild().getTestOutputDirectory()).toPath()));
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
	
	private IJavadocProvider createRoasterJavadocProvider(File classpathResource) {
		if (classpathResource.isDirectory()) {
			if (classpathResource.toString().startsWith(project.getBuild().getOutputDirectory())) {
				return new RoasterJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getBuild().getSourceDirectory()).toURI().toURL(), type);
				});
			} else if (classpathResource.toString().startsWith(project.getBuild().getTestOutputDirectory())) {
				return new RoasterJavadocProvider(type -> {
					return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
							.sourceUrl(new File(project.getBuild().getTestSourceDirectory()).toURI().toURL(), type);
				});
			} else {
				throw new IllegalArgumentException("Cannot find source folder for " + classpathResource);
			}
		} else {
			// Assume it's a JAR file
			return new RoasterJavadocProvider(type -> {
				try {
					Artifact artifact = getArtifactFromJarFile(classpathResource).get();
					URL sourceContainer = maven.getSources(artifact).getFile().toURI().toURL();
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

	private IJavadocProvider createParserJavadocProvider(File classpathResource) {
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
					URL sourceContainer = maven.getSources(artifact).getFile().toURI().toURL();
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
					URL sourceContainer = maven.getJavadoc(artifact).getFile().toURI().toURL();
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
