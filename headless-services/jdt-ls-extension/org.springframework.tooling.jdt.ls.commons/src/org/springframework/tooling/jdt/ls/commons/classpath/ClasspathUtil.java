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

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.springframework.tooling.jdt.ls.commons.classpath.Classpath.CPE;

public class ClasspathUtil {

	public static Classpath resolve(IJavaProject javaProject) throws Exception {

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
		log("classpath=" + classpath.getEntries().size() + " entries");
		return classpath;
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
