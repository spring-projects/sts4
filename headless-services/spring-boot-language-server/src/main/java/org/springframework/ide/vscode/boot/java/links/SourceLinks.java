/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IClasspathUtil;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.javadoc.TypeUrlProviderFromContainerUrl;

/**
 * Instance is able to provide client specific URL links to navigate to a
 * specific file on the client given various types of data such as fully
 * qualified java type name or classpath resource or just some file resource
 * given by its path.
 *
 * @author Alex Boyko
 *
 */
public interface SourceLinks {

	static final Logger log = LoggerFactory.getLogger(SourceLinks.class);

	static final String JAR = ".jar";
	static final String CLASS = ".class";

	public static Optional<Path> sourceFromSourceFolder(String fqName, IClasspath classpath) {
		return IClasspathUtil.getSourceFolders(classpath)
			.map(sourceFolder -> {
				try {
					return sourceFolder.toURI().toURL();
				} catch (MalformedURLException e) {
					log.warn("Failed to convert source folder {} to URI {}", sourceFolder, fqName, e);
					return null;
				}
			})
			.map(url -> {
				try {
					return TypeUrlProviderFromContainerUrl.SOURCE_FOLDER_URL_SUPPLIER.url(url, fqName, /* module */ null);
				} catch (Exception e) {
					log.warn("Failed to determine source URL from url={} fqName=", url, fqName, e);
					return null;
				}
			})
			.map(url -> {
				try {
					return Paths.get(url.toURI());
				} catch (URISyntaxException e) {
					log.warn("Failed to convert URL {} to path. {}", url, fqName, e);
					return null;
				}
			})
			.filter(sourcePath -> sourcePath != null && Files.exists(sourcePath))
			.findFirst();
	}

	public static Optional<URL> source(IJavaProject project, String fqName) {
		// Try to find in a source JAR
		IJavaModuleData classpathResourceContainer = project.findClasspathResourceContainer(fqName);
		if (classpathResourceContainer != null) {
			Optional<URL> url = project.sourceContainer(classpathResourceContainer.getContainer()).map(file -> {
				try {
					return TypeUrlProviderFromContainerUrl.JAR_SOURCE_URL_PROVIDER.url(file, fqName, classpathResourceContainer.getModule());
				} catch (Exception e) {
					throw new IllegalStateException(e);
				}
			});

			if (!url.isPresent()) {
				// Try Source folder
				url = sourceFromSourceFolder(fqName, project.getClasspath()).map(p -> {
					try {
						return p.toUri().toURL();
					} catch (MalformedURLException e) {
						throw new IllegalStateException(e);
					}
				});
			}
			return url;
		}
		return Optional.empty();
	}


	/**
	 * Creates link to source file defining the type passed with it's fully qualified name
	 * @param project Java project in the context of which source file link is calculated
	 * @param fqName type's fully qualified name
	 * @return the link URL optional
	 */
	Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName);

	/**
	 * Creates link to source file corresponding to a classpath resource
	 * @param project project Java project in the context of which source file link is calculated
	 * @param path the path to the classpath resource
	 * @return the link URL optional
	 */
	Optional<String> sourceLinkUrlForClasspathResource(IJavaProject project, String path);

	/**
	 * Creates link to a file specified by it's path
	 * @param path the resource path
	 * @return the link URL optional
	 */
	Optional<String> sourceLinkForResourcePath(Path path);

}
