/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.live.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.beans.ui.live.LiveBeansUiPlugin;

/**
 * JDT Utilities (subset taken from Spring-IDE core to minimize dependencies) 
 * 
 * @author Alex Boyko
 *
 */
public class JdtUtils {

	/**
	 * Returns the corresponding Java project or <code>null</code> a for given
	 * project.
	 * 
	 * @param project
	 *            the project the Java project is requested for
	 * @return the requested Java project or <code>null</code> if the Java project
	 *         is not defined or the project is not accessible
	 */
	public static IJavaProject getJavaProject(IProject project) {
		if (project.isAccessible()) {
			try {
				if (project.hasNature(JavaCore.NATURE_ID)) {
					return (IJavaProject) project.getNature(JavaCore.NATURE_ID);
				}
			} catch (CoreException e) {
				LiveBeansUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, LiveBeansUiPlugin.PLUGIN_ID,
						"Error getting Java project for project '" + project.getName() + "'", e));
			}
		}
		return null;
	}

	/**
	 * Returns the corresponding Java type for given full-qualified class name.
	 * 
	 * @param project
	 *            the JDT project the class belongs to
	 * @param className
	 *            the full qualified class name of the requested Java type
	 * @return the requested Java type or null if the class is not defined or the
	 *         project is not accessible
	 */
	public static IType getJavaType(IProject project, String className) {
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (className != null) {
			// For inner classes replace '$' by '.'
			int pos = className.lastIndexOf('$');
			if (pos > 0) {
				className = className.replace('$', '.');
			}
			try {
				IType type = null;
				// First look for the type in the Java project
				if (javaProject != null) {
					// TODO CD not sure why we need
					type = javaProject.findType(className, new NullProgressMonitor());
					// type = javaProject.findType(className);
					if (type != null) {
						return type;
					}
				}

				// Then look for the type in the referenced Java projects
				for (IProject refProject : project.getReferencedProjects()) {
					IJavaProject refJavaProject = JdtUtils.getJavaProject(refProject);
					if (refJavaProject != null) {
						type = refJavaProject.findType(className);
						if (type != null) {
							return type;
						}
					}
				}

				// fall back and try to locate the class using AJDT
				// TODO: uncomment this call
				// return getAjdtType(project, className);
			} catch (CoreException e) {
				LiveBeansUiPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, LiveBeansUiPlugin.PLUGIN_ID,
						"Error getting Java type '" + className + "'", e));
			}
		}

		return null;
	}

}
