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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServersRegistry;
import org.eclipse.lsp4e.LanguageServiceAccessor;
import org.eclipse.ui.IStartup;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.classpath.ClasspathListenerManager;

@SuppressWarnings("restriction")
public class Startup implements IStartup {
	
	private static final String BOOT_LS_DEFINITION_ID = "org.eclipse.languageserver.languages.springboot";
	
	private Set<IJavaProject> springProjects = new HashSet<>();
	private LanguageServerWrapper lsWrapper = null;

	@Override
	public void earlyStartup() {
		new ClasspathListenerManager(Logger.forEclipsePlugin(() -> BootLanguageServerPlugin.getDefault()), new ClasspathListenerManager.ClasspathListener() {
			
			@Override
			public void classpathChanged(IJavaProject jp) {
				processProject(jp);
			}
		});
		for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (p != null && p.isAccessible()) {
				IJavaProject jp = JavaCore.create(p);
				if (jp != null && jp.exists()) {
					processProject(jp);
				}
			}
		}
	}
	
	private void processProject(IJavaProject jp) {
		if (isSpringProject(jp)) {
			springProjects.add(jp);
		} else {
			springProjects.remove(jp);
		}
		if (springProjects.isEmpty()) {
			if (lsWrapper != null) {
				lsWrapper.stop();
				lsWrapper = null;
			}
		} else {
			if (lsWrapper == null) {
				try {
					lsWrapper = LanguageServiceAccessor.startLanguageServer(LanguageServersRegistry.getInstance().getDefinition(BOOT_LS_DEFINITION_ID));
				} catch (IOException e) {
					BootLanguageServerPlugin.getDefault().getLog().error("Failed to launch Boot Language Server for " + jp.getElementName(), e);
				}

			}
		}
	}
	
	private boolean isSpringProject(IJavaProject jp) {
		try {
			IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
			//Look for a 'spring-core' jar or project entry
			for (IClasspathEntry e : classpath) {
				if (isBootJar(e) || isBootProject(e)) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			BootLanguageServerPlugin.getDefault().getLog().error("Cannot compute classpath for project " + jp.getElementName(), e);
		}
		return false;
	}
	
	public static boolean isBootProject(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_PROJECT) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.startsWith("spring-core");
		}
		return false;
	}

	public static boolean isBootJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-core");
		}
		return false;
	}


}
