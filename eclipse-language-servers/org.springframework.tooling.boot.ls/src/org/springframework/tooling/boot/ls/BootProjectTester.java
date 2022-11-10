/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

@SuppressWarnings("restriction")
public class BootProjectTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("isBootResource".equals(property)) {
			IResource resource = null;
			if (receiver instanceof IAdaptable) {
				resource = ((IAdaptable) receiver).getAdapter(IResource.class);
			} else if (receiver instanceof IDocument) {
				resource = LSPEclipseUtils.getFile((IDocument) receiver);
			}
			if (resource != null) {
				IProject project = resource.getProject();
				if (project != null) {
					IJavaProject jp = JavaCore.create(project);
					if (jp != null) {
						return isBootProject(project);
					}
				}
			}
		}
		return false;
	}
	
	private static boolean isBootProject(IProject project) {
		if (project==null || ! project.isAccessible()) {
			return false;
		}
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
					IJavaProject jp = JavaCore.create(project);
					IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
					//Look for a 'spring-boot' jar or project entry

					for (IClasspathEntry e : classpath) {
						if (isBootJar(e) || isBootProject(e)) {
							return true;
						}
					}
			}
		} catch (Exception e) {
			CorePlugin.log(e);
		}
		return false;
	}

	private static boolean isBootJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot");
		}
		return false;
	}
	
	private static boolean isBootProject(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_PROJECT) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.startsWith("spring-boot");
		}
		return false;
	}

}
