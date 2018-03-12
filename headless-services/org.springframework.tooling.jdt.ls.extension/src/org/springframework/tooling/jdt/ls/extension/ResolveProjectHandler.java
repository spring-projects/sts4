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
package org.springframework.tooling.jdt.ls.extension;

import static org.springframework.tooling.jdt.ls.extension.Logger.log;

import java.net.URI;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

@SuppressWarnings("restriction")
public class ResolveProjectHandler implements IDelegateCommandHandler {

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {

		log("ResolveProjectHandler=" + commandId);
		try {
			URI resourceUri = ResourceUtils.getResourceUri(arguments);
			
			log("resourceUri=" + resourceUri);
			
			IJavaProject javaProject = ResourceUtils.getJavaProject(resourceUri);

			ProjectResponse projectResponse = new ProjectResponse(javaProject.getElementName(), javaProject.getProject().getLocationURI().toString());
			
			log("projectResponse="+projectResponse);
			
			return projectResponse;
		} catch (Exception e) {
			log(e);
			throw e;
		}
	}

	public class ProjectResponse {

		private String name;
		private String uri;

		public ProjectResponse(String name, String uri) {
			super();
			this.name = name;
			this.uri = uri;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		@Override
		public String toString() {
			return "ProjectResponse [name=" + name + ", uri=" + uri + "]";
		}

	}

}
