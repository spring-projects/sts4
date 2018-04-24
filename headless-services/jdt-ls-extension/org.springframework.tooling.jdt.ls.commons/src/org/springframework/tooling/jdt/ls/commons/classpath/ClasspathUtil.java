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
import static org.springframework.tooling.jdt.ls.commons.Logger.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;

public class ClasspathUtil {

	public static Classpath resolve(IJavaProject javaProject) throws Exception {
		//log("resolving classpath " + javaProject.getElementName() +" ...");

		List<CPE> cpEntries = new ArrayList<>();
		IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);

		if (entries != null) {
			for (IClasspathEntry entry : entries) {
				String kind = toContentKind(entry);
				switch (kind) {
				case Classpath.ENTRY_KIND_BINARY: {
					String path = entry.getPath().toString();
					CPE cpe = new CPE(kind, path);
					cpEntries.add(cpe);
					break;
				}
				case Classpath.ENTRY_KIND_SOURCE: {
					IPath sourcePath = entry.getPath();
					//log("source entry =" + sourcePath);
					IPath absoluteSourcePath = resolveWorkspacePath(sourcePath);
					//log("absoluteSourcePath =" + absoluteSourcePath);
					if (absoluteSourcePath!=null) {
						CPE cpe = new CPE(kind, absoluteSourcePath.toString());
						IPath of = entry.getOutputLocation();
						//log("outputFolder =" + of);
						if (of!=null) {
							IPath absoluteOutFolder = resolveWorkspacePath(of);
							//log("absoluteOutFolder =" + absoluteOutFolder);
							cpe.setOutputFolder(absoluteOutFolder.toString());
						}
						cpEntries.add(cpe);
					}
					break;
				}
				default:
					break;
				}
			}
		}
		Classpath classpath = new Classpath(cpEntries, resolveWorkspacePath(javaProject.getOutputLocation()).toString());
		log("classpath.outputFolder=" + classpath.getDefaultOutputFolder());
		log("classpath=" + classpath.getEntries().size() + " entries");
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
