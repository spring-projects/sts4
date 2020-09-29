/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.springframework.ide.eclipse.xml.namespaces.classpath.ProjectResourceLoaderCache;
import org.springframework.ide.eclipse.xml.namespaces.classpath.ResourceLoader;
import org.springsource.ide.eclipse.commons.core.JdtUtils;

/**
 * Support for "project-aware" URL protocol. The protocol is for resources
 * within JARs. The JARs are located within a Spring project.
 * 
 * @author Alex Boyko
 *
 */
public class ProjectAwareUrlStreamHandlerService extends
		AbstractURLStreamHandlerService {
	
	public static final String PROJECT_AWARE_PROTOCOL = "project-aware";
	public static final String PROJECT_AWARE_PROTOCOL_HEADER = PROJECT_AWARE_PROTOCOL + "://";

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		String systemId = u.toString();
		String nameAndLocation = systemId.substring(PROJECT_AWARE_PROTOCOL_HEADER.length());
		String projectName = nameAndLocation.substring(0, nameAndLocation.indexOf('/'));
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		String resourceId = nameAndLocation.substring(nameAndLocation.indexOf('/') + 1);
		ResourceLoader cl = ProjectResourceLoaderCache.getResourceLoader(project, null);
		URL resource = cl.getResource(resourceId);
		if (resource != null) {
			return resource.openConnection();
		}
		return null;
	}
	
	/**
	 * Creates a string representation of a project-aware protocol URL from
	 * project name and a resource name
	 * 
	 * @param projectName
	 *            spring project name
	 * @param resourceName
	 *            class loader resource name
	 * @return URL string
	 */
	public static String createProjectAwareUrl(String projectName, String resourceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL_HEADER);
		sb.append(projectName);
		sb.append('/');
		sb.append(resourceName);
		return sb.toString();
	}

}
