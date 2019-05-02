/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;

/**
 * @author Martin Lippert
 */
@SuppressWarnings("restriction")
public class JavaCodeCompletionUtils {

	public static final String CLASS_NAME = "_xxx";

	private static final String CLASS_SOURCE_START = "public class " + CLASS_NAME + " {\n"
			+ "    public void main(String[] args) {\n" + "        ";

	private static final String CLASS_SOURCE_END = "\n" + "    }\n" + "}";

	
	public static void codeComplete(IJavaProject project, String prefix, JavaCodeCompletionProposalCollector proposalCollector) {
		if (prefix == null || prefix.length() == 0) {
			return;
		}

		try {
			ICompilationUnit unit = createCompilationUnit(project, prefix);

			prefix = prefix.replace('$', '.');

			String sourceStart = CLASS_SOURCE_START + prefix;
			String packageName = null;

			int dot = prefix.lastIndexOf('.');
			if (dot > -1) {
				packageName = prefix.substring(0, dot);
				sourceStart = "package " + packageName + ";\n" + sourceStart;
			}

			String source = sourceStart + CLASS_SOURCE_END;
			setContents(unit, source);

			unit.codeComplete(sourceStart.length(), proposalCollector, DefaultWorkingCopyOwner.PRIMARY);
		}
		catch (Exception e) {
			// do nothing
		}
	}

	private static ICompilationUnit createCompilationUnit(IJavaProject project, String prefix) throws JavaModelException {
		IPackageFragment root = getPackageFragment(project, prefix);
		ICompilationUnit unit = root.getCompilationUnit("_xxx.java").getWorkingCopy(null);
		return unit;
	}

	private static IPackageFragment getPackageFragment(IJavaProject project, String prefix) throws JavaModelException {
		int dot = prefix.lastIndexOf('.');
		if (dot > -1) {
			String packageName = prefix.substring(0, dot);
			for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
				IPackageFragment p = root.getPackageFragment(packageName);
				if (p != null && p.exists()) {
					return p;
				}
			}
			IPackageFragment[] packages = project.getPackageFragments();
			for (IPackageFragment p : packages) {
				if (p.getElementName().equals(packageName))
					return p;
			}
		}
		else {
			for (IPackageFragmentRoot p : project.getAllPackageFragmentRoots()) {
				if (p.getKind() == IPackageFragmentRoot.K_SOURCE) {
					return p.getPackageFragment("");
				}
			}
		}
		return project.getPackageFragments()[0];
	}

	private static void setContents(ICompilationUnit cu, String source) {
		if (cu == null)
			return;

		synchronized (cu) {
			IBuffer buffer;
			try {

				buffer = cu.getBuffer();
			}
			catch (JavaModelException e) {
				e.printStackTrace();
				buffer = null;
			}

			if (buffer != null)
				buffer.setContents(source);
		}
	}

}
