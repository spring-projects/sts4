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

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ls.core.internal.IDelegateCommandHandler;

@SuppressWarnings("restriction")
public class ResolveClasspathHandler implements IDelegateCommandHandler {

	public static final String ENTRY_CONTENT_SOURCE = "source";
	public static final String ENTRY_CONTENT_BINARY = "binary";
	public static final String OUTPUT_LOCATION = "output_location";

	@Override
	public Object executeCommand(String commandId, List<Object> arguments, IProgressMonitor monitor) throws Exception {

		try {
			URI resourceUri = getResourceUri(arguments);

			IJavaProject javaProject = getJavaProject(resourceUri);

			return resolveClasspathHandler(javaProject);
		} catch (Exception e) {
			log(e);
			throw e;
		}
	}

	private Classpath resolveClasspathHandler(IJavaProject javaProject) throws Exception {

		List<CPE> cpEntries = new ArrayList<>();
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);

		if (entries != null) {
			for (IClasspathEntry entry : entries) {
				String kind = toContentKind(entry);
				String path = entry.getPath().toString();
				cpEntries.add(new CPE(kind, path));
			}
		}
		Classpath classpath = new Classpath(cpEntries, javaProject.getOutputLocation().toString());
		log("classpath=" + classpath);
		return classpath;
	}

	private String toContentKind(IClasspathEntry entry) {
		switch (entry.getContentKind()) {
		case IPackageFragmentRoot.K_BINARY:
			return ENTRY_CONTENT_BINARY;
		case IPackageFragmentRoot.K_SOURCE:
			return ENTRY_CONTENT_SOURCE;
		default:
			return "unknown: " + entry.getContentKind();
		}
	}

	private IJavaProject getJavaProject(URI resourceUri) throws Exception {
		IContainer[] containers = ResourcesPlugin.getWorkspace().getRoot().findContainersForLocationURI(resourceUri);
		log("containers=" + containers);
		if (containers.length > 0) {
			log("containers.length=" + containers.length);
			Optional<IContainer> shortest = Arrays.stream(containers)
					.min((f1, f2) -> f1.getFullPath().segmentCount() - f2.getFullPath().segmentCount());
			log("shortest=" + shortest.isPresent());

			if (shortest.isPresent()) {
				log("shortest.fullpath=" + shortest.get().getFullPath());

				IProject project = shortest.get().getProject();
				log("project=" + project.getName());

				if (project.isAccessible() && project.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject javaProject = JavaCore.create(project);
					log("javaProject=" + javaProject.getElementName());

					return javaProject;
				}
			}
		}

		throw new Exception("Resolve classpath: unable to resolve a Java project from : " + resourceUri);
	}

	private URI getResourceUri(List<Object> arguments) throws Exception {
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

	public static class CPE {
		private String kind;
		private String path;

		public CPE(String kind, String path) {
			super();
			this.kind = kind;
			this.path = path;
		}

		public String getKind() {
			return kind;
		}

		public void setKind(String kind) {
			this.kind = kind;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return "CPE [kind=" + kind + ", path=" + path + "]\n";
		}

	}

	public static class Classpath {

		private List<CPE> entries;
		private String defaultOutputFolder;

		public Classpath(List<CPE> entries, String defaultOutputFolder) {
			super();
			this.entries = entries;
			this.defaultOutputFolder = defaultOutputFolder;
		}

		public List<CPE> getEntries() {
			return entries;
		}

		public void setEntries(List<CPE> entries) {
			this.entries = entries;
		}

		public String getDefaultOutputFolder() {
			return defaultOutputFolder;
		}

		public void setDefaultOutputFolder(String defaultOutputFolder) {
			this.defaultOutputFolder = defaultOutputFolder;
		}

		@Override
		public String toString() {
			return "Classpath [entries=" + entries + ", defaultOutputFolder=" + defaultOutputFolder + "]";
		}

	}

}
