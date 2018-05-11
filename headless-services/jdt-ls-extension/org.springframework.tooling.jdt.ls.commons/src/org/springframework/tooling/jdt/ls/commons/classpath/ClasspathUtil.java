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
package org.springframework.tooling.jdt.ls.commons.classpath;

import static org.springframework.tooling.jdt.ls.commons.classpath.Classpath.ENTRY_KIND_BINARY;
import static org.springframework.tooling.jdt.ls.commons.classpath.Classpath.ENTRY_KIND_SOURCE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaProject;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;

public class ClasspathUtil {

	private static final Object JRE_CONTAINER_ID = "org.eclipse.jdt.launching.JRE_CONTAINER";

	private static Set<String> getSystemLibraryPaths(IJavaProject javaProject) {
		try {
			IClasspathEntry jreContainer = getJreContainer(javaProject.getRawClasspath());
			IClasspathEntry[] resolvedJreEntries = ((JavaProject)javaProject).resolveClasspath(new IClasspathEntry[] {jreContainer});
			Set<String> paths = new HashSet<>();
			for (IClasspathEntry systemEntry : resolvedJreEntries) {
				paths.add(systemEntry.getPath().toString());
			}
			return paths;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Collections.emptySet();
	}
	
	private static IClasspathEntry getJreContainer(IClasspathEntry[] rawClasspath) {
		for (IClasspathEntry cpe : rawClasspath) {
			if (cpe.getEntryKind()==IClasspathEntry.CPE_CONTAINER) {
				if (cpe.getPath().segment(0).equals(JRE_CONTAINER_ID)) {
					return cpe;
				}
			}
		}
		return null;
	}

	public static Classpath resolve(IJavaProject javaProject, Logger logger) throws Exception {
		//log("resolving classpath " + javaProject.getElementName() +" ...");

		List<CPE> cpEntries = new ArrayList<>();
		

		
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);
		Set<String> systemLibs = getSystemLibraryPaths(javaProject);

		if (entries != null) {
			for (IClasspathEntry entry : entries) {
				String kind = toContentKind(entry);
				switch (kind) {
				case Classpath.ENTRY_KIND_BINARY: {
					String path = entry.getPath().toString();
					CPE cpe = CPE.binary(path);
					if (systemLibs.contains(path)) {
						cpe.setSystem(true);
					}
					cpEntries.add(cpe);
					break;
				}
				case Classpath.ENTRY_KIND_SOURCE: {
					IPath sourcePath = entry.getPath();
					//log("source entry =" + sourcePath);
					IPath absoluteSourcePath = resolveWorkspacePath(sourcePath);
					//log("absoluteSourcePath =" + absoluteSourcePath);
					if (absoluteSourcePath!=null) {
						IPath of = entry.getOutputLocation();
						//log("outputFolder =" + of);
						IPath absoluteOutFolder;
						if (of!=null) {
							absoluteOutFolder = resolveWorkspacePath(of);
						} else {
							absoluteOutFolder = resolveWorkspacePath(javaProject.getOutputLocation());
						}
						cpEntries.add(CPE.source(absoluteSourcePath.toFile(), absoluteOutFolder.toFile()));
					}
					break;
				}
				default:
					break;
				}
			}
		}
		Classpath classpath = new Classpath(cpEntries);
		logger.log("classpath=" + classpath.getEntries().size() + " entries");
		return classpath;
	}
	
	private static IPath resolveWorkspacePath(IPath path) {
		if (path.segmentCount()>0) {
			String projectName = path.segment(0);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IPath projectRoot = project.getLocation();
			if (projectRoot!=null) {
				return projectRoot.append(path.removeFirstSegments(1));
			}
		}
		return null;
	}
	
	private static String toContentKind(IClasspathEntry entry) {
		switch (entry.getContentKind()) {
		case IPackageFragmentRoot.K_BINARY:
			return ENTRY_KIND_BINARY;
		case IPackageFragmentRoot.K_SOURCE:
			return ENTRY_KIND_SOURCE;
		default:
			return "unknown: " + entry.getContentKind();
		}
	}
}
