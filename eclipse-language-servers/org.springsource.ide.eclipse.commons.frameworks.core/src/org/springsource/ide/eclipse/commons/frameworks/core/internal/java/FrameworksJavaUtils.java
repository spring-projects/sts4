/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.frameworks.core.internal.java;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.springsource.ide.eclipse.commons.frameworks.core.FrameworkCoreActivator;


/**
 * Common Java utility methods.
 * @author Nieraj Singh
 */
public class FrameworksJavaUtils {

	private FrameworksJavaUtils() {
		// Utility class
	}

	/**
	 * Obtain the package fragment roots of the first encountered source class
	 * path entry in the given java project. Return null if the java project is
	 * null, or no source class path entries can be found.
	 * 
	 * @param javaProject
	 *           containing source class path entries
	 * @return package fragment roots of first encountered source class path
	 *        entry, or null otherwise
	 */
	public static IPackageFragmentRoot[] getFirstEncounteredSourcePackageFragmentRoots(
			IJavaProject javaProject) {
		if (javaProject == null) {
			return null;
		}

		try {

			IClasspathEntry[] entries = javaProject.getRawClasspath();

			for (IClasspathEntry entry : entries) {

				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					return javaProject.findPackageFragmentRoots(entry);
				}
			}

		} catch (JavaModelException e) {
			FrameworkCoreActivator.logError(e.getLocalizedMessage(), e);
		}
		return null;

	}

}
