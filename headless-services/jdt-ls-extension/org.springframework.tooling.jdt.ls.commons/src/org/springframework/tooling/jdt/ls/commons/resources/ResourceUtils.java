/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.resources;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public final class ResourceUtils {

	/**
	 * Resolves a Java project given the resource URI. The resource URI could be any resource contained in the project.
	 * @param resourceUri
	 * @return Java project
	 * @throws Exception if unable to resolve Java project from given resource URI
	 */
	public static IJavaProject getJavaProject(URI resourceUri) throws Exception {
		IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(resourceUri);
//		log("containers=" + containers);
		if (containers.length > 0) {
//			log("containers.length=" + containers.length);
			Optional<IContainer> shortest = Arrays.stream(containers)
					.min((f1, f2) -> f1.getFullPath().segmentCount() - f2.getFullPath().segmentCount());
//			log("shortest=" + shortest.isPresent());

			if (shortest.isPresent()) {
//				log("shortest.fullpath=" + shortest.get().getFullPath());

				IProject project = shortest.get().getProject();
//				log("project=" + project.getName());

				if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);
//					log("javaProject=" + javaProject.getElementName());

					return javaProject;
				}
			}
		}

		throw new Exception("Resolve classpath: unable to resolve a Java project from : " + resourceUri);
	}
	
	public static Collection<IJavaProject> allJavaProjects() {
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		ArrayList<IJavaProject> javaProjects = new ArrayList<>(projects.length);
		for (IProject project : projects) {
			try {
				if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);
					if (javaProject != null) {
						javaProjects.add(javaProject);
					}
				}
			} catch (CoreException e) {
				// ignore
			}
		}
		return javaProjects;
	}

	/**
	 * Returns the resource URI, IFF it exists. Throws exception if it doesn't exist.
	 * @param arguments
	 * @return
	 * @throws Exception
	 */
	public static URI getResourceUri(List<Object> arguments) throws Exception {
		if (arguments == null || arguments.size() == 0) {
			throw new IllegalArgumentException(
					"Resolve classpath: Invalid number of arguments. A resource URI is required.");
		}
		URI uri = URI.create((String) arguments.get(0));
		File resource = new File(uri);
		if (!resource.exists()) {
			throw new IllegalArgumentException("Resolve classpath: Resource does not exist: " + resource);
		}

		return uri;
	}

}
