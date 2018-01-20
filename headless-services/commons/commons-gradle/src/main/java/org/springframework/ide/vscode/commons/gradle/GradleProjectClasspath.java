/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gradle.tooling.model.build.BuildEnvironment;
import org.gradle.tooling.model.eclipse.EclipseProject;
import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.jandex.JandexIndex;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.java.parser.ParserJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.HtmlJavadocProvider;
import org.springframework.ide.vscode.commons.javadoc.SourceUrlProviderFromSourceContainer;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.collect.ImmutableList;

/**
 * Implementation of {@link IClasspath} for Gradle projects
 * 
 * @author Alex Boyko
 *
 */
public class GradleProjectClasspath extends JandexClasspath {
	
	private static final String JAVA_HOME = "java.home";
	private static final String JAVA_RUNTIME_VERSION = "java.runtime.version";
	private static final String JAVA_BOOT_CLASS_PATH = "sun.boot.class.path";
	
	private EclipseProject project;
	private BuildEnvironment buildEnvironment;
	
	public GradleProjectClasspath(GradleCore gradle, File projectDir) throws GradleException {
		super();
		this.project = gradle.getModel(projectDir, EclipseProject.class);
		this.buildEnvironment = gradle.getModel(projectDir, BuildEnvironment.class);
	}

	@Override
	protected JandexIndex[] getBaseIndices() {
		return new JandexIndex[] { new JandexIndex(getJreLibs().map(path -> path.toFile()).collect(Collectors.toList()),
				jarFile -> findIndexFile(jarFile), (classpathResource) -> {
					try {
						String javaVersion = getJavaRuntimeMinorVersion();
						if (javaVersion == null) {
							javaVersion = "8";
						}
						URL javadocUrl = new URL("https://docs.oracle.com/javase/" + javaVersion + "/docs/api/");
						return new HtmlJavadocProvider(
								(type) -> SourceUrlProviderFromSourceContainer.JAVADOC_FOLDER_URL_SUPPLIER
										.sourceUrl(javadocUrl, type));
					} catch (MalformedURLException e) {
						Log.log(e);
						return null;
					}
				}) };
	}
	
	public EclipseProject getRootProject() {
		EclipseProject root = project;
		if (root == null) {
			return root;
		}
		while(root.getParent() != null) {
			root = root.getParent();
		}
		return root;
	}

	@Override
	public ImmutableList<Path> getClasspathEntries() throws Exception {
		EclipseProject root = getRootProject();
		if (project == null) {
			return ImmutableList.of();
		} else {
			ImmutableList<Path> classpathEntries = ImmutableList.copyOf(Stream.concat(project.getClasspath().stream().map(dep -> dep.getFile().toPath()),
					project.getProjectDependencies().stream()
						.map(d -> findPeer(root, d.getTargetProject().getName()))
						.filter(o -> o.isPresent())
						.map(o -> o.get())
						.map(p -> p.getProjectDirectory().toPath().resolve(p.getOutputLocation().getPath()))
				).collect(Collectors.toList()));
			return classpathEntries;
		}
	}
	
	private Optional<? extends EclipseProject> findPeer(EclipseProject root, String name) {
		return root.getChildren().stream().filter(p -> p.getName().equals(name)).findFirst();
	}
	
	@Override
	public ImmutableList<String> getClasspathResources() {
		if (project == null) {
			return ImmutableList.of();
		} else {
			return ImmutableList.copyOf(project.getSourceDirectories().stream().map(sourceDirectory -> sourceDirectory.getDirectory()).flatMap(folder -> {
				try {
					return Files.walk(folder.toPath())
							.filter(path -> Files.isRegularFile(path))
							.map(path -> folder.toPath().relativize(path))
							.map(relativePath -> relativePath.toString())
							.filter(pathString -> !pathString.endsWith(".java") && !pathString.endsWith(".class"));
				} catch (IOException e) {
					return Stream.empty();
				}
			}).toArray(String[]::new));
		}
	}

	public Path getOutputFolder() {
		return project == null ? null : project.getProjectDirectory().toPath().resolve(project.getOutputLocation().getPath());
	}

	public String getName() {
		return project == null ? null : project.getName();
	}

	public boolean exists() {
		return project != null;
	}

	@Override
	protected IJavadocProvider createParserJavadocProvider(File classpathResource) {
		if (project != null) {
			if (classpathResource.isDirectory()) {
				Optional<File> classpathFolder = project.getSourceDirectories().stream()
						.map(dir -> dir.getDirectory())
						.filter(dir -> classpathResource.toPath().startsWith(dir.toPath()))
						.findFirst();
				if (classpathFolder.isPresent()) {
					return new ParserJavadocProvider(type -> {
						return SourceUrlProviderFromSourceContainer.SOURCE_FOLDER_URL_SUPPLIER
								.sourceUrl(classpathFolder.get().toURI().toURL(), type);
					});

				}
			} else {
				
			}
		}
		return null;
	}

	@Override
	protected IJavadocProvider createHtmlJavdocProvider(File classpathResource) {
		return null;
	}
	
	public String getGradleVersion()  throws GradleException {
		if (buildEnvironment == null) {
			throw new GradleException(new Exception("Cannot find Gradle version"));
		} else {
			return buildEnvironment.getGradle().getGradleVersion();
		}
	}
	
	public File getGradleHome() throws GradleException {
		if (buildEnvironment == null) {
			throw new GradleException(new Exception("Cannot find Gradle home folder"));
		} else {
			return buildEnvironment.getGradle().getGradleUserHome();
		}
	}
	
	public String getJavaRuntimeVersion() {
		return System.getProperty(JAVA_RUNTIME_VERSION);
	}
	
	public String getJavaRuntimeMinorVersion() {
		String fullVersion = getJavaRuntimeVersion();
		String[] tokenized = fullVersion.split("\\.");
		if (tokenized.length > 1) {
			return tokenized[1];
		} else {
			Log.log("Cannot determine minor version for the Java Runtime Version: " + fullVersion);
			return null;
		}
	}
	
	private String getJavaHome() {
		if (buildEnvironment == null) {
			return System.getProperty(JAVA_HOME);
		} else {
			return buildEnvironment.getJava().getJavaHome().toString();
		}
	}
	
	private Stream<Path> getJreLibs() {
		String s = System.getProperty(JAVA_BOOT_CLASS_PATH);
		return Arrays.stream(s.split(File.pathSeparator))
				.map(strPath -> strPath.replace(System.getProperty(JAVA_HOME), getJavaHome()))
				.map(File::new)
				.filter(f -> f.canRead())
				.map(f -> f.toPath());
	}
	
	private File findIndexFile(File jarFile) {
		String suffix = null;
			String javaHome = getJavaHome();
			if (javaHome != null) {
				int index = javaHome.lastIndexOf('/');
				if (index != -1) {
					javaHome = javaHome.substring(0, index);
				}
			}
			if (jarFile.toString().startsWith(javaHome)) {
				suffix = getJavaRuntimeVersion();
			}
		return new File(JandexIndex.getIndexFolder().toString(), jarFile.getName() + "-" + suffix + ".jdx");
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GradleProjectClasspath) {
			return super.equals(obj);
		}
		return false;
	}

	@Override
	public ImmutableList<String> getSourceFolders() {
		return ImmutableList.copyOf(project.getSourceDirectories().stream()
				.map(dir -> dir.getDirectory().toPath().toString()).collect(Collectors.toList()));
	}

}
