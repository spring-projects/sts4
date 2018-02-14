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

import java.nio.file.Path;
import java.util.Optional;

import org.springframework.ide.vscode.commons.java.IJavaProject;

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
	
	static final String JAR = ".jar";
	static final String CLASS = ".class";

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
