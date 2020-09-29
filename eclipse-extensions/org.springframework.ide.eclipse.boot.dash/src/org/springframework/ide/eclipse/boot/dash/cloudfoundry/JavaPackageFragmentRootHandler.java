/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Resolves all the package fragment roots and main type for a give Java
 * project. It includes handling all required Java projects for the given java
 * project.
 *
 */
public class JavaPackageFragmentRootHandler {

	private IJavaProject javaProject;

	private IType type;

	public JavaPackageFragmentRootHandler(IJavaProject javaProject) {
		this.javaProject = javaProject;
	}

	public IPackageFragmentRoot[] getPackageFragmentRoots(IProgressMonitor monitor) throws Exception {

		IPath[] classpathEntries = null;
		ILaunchConfiguration configuration = null;
		try {
			if (BootPropertyTester.isBootProject(javaProject.getProject())) {
				configuration = BootLaunchConfigurationDelegate.createConf(javaProject);
				Set<String> modes = new HashSet<String>();
				modes.add(ILaunchManager.RUN_MODE);

				ILaunchDelegate[] delegates = configuration.getType().getDelegates(modes);
				if (delegates != null && delegates.length > 0) {

					JavaLaunchDelegate javaDelegate = null;

					for (ILaunchDelegate del : delegates) {
						if (BootLaunchConfigurationDelegate.TYPE_ID.equals(del.getId())
								&& del.getDelegate() instanceof JavaLaunchDelegate) {

							javaDelegate = (JavaLaunchDelegate) del.getDelegate();
							break;
						}
					}

					if (javaDelegate != null) {
						String[] pathValues = javaDelegate.getClasspath(configuration);
						if (pathValues != null) {
							classpathEntries = new IPath[pathValues.length];
							for (int i = 0; i < pathValues.length && i < classpathEntries.length; i++) {
								classpathEntries[i] = new Path(pathValues[i]);
							}
						}
					}
				}

			} else {
				throw ExceptionUtil.coreException("The project : " + javaProject.getProject().getName()
						+ " does not appear to be a Spring Boot project. Only Spring Boot projects can be deployed through the Boot Dashboard.");
			}

		} finally {
			if (configuration != null) {
				configuration.delete();
			}
		}

		if (classpathEntries != null && classpathEntries.length > 0) {
			List<IPackageFragmentRoot> pckRoots = new ArrayList<IPackageFragmentRoot>();

			// Since the java project may have other required projects, fetch
			// the
			// ordered
			// list of required projects that will be used to search for package
			// fragment roots
			// corresponding to the resolved class paths. The order of the java
			// projects to search in should
			// start with the most immediate list of required projects of the
			// java
			// project.
			List<IJavaProject> javaProjectsToSearch = getOrderedJavaProjects(javaProject);

			// Find package fragment roots corresponding to the path entries.
			// Search through all java projects, not just the immediate java
			// project
			// for the application that is being pushed to CF.
			for (IPath path : classpathEntries) {

				for (IJavaProject javaProject : javaProjectsToSearch) {

					IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();

					if (roots != null) {
						List<IPackageFragmentRoot> foundRoots = new ArrayList<IPackageFragmentRoot>();

						for (IPackageFragmentRoot packageFragmentRoot : roots) {
							// Note that a class path entry may correspond to a
							// Java project's target directory.
							// Different fragment roots may use the same target
							// directory, so relationship between fragment roots
							// to a class path entry may be many-to-one.

							if (isRootAtEntry(packageFragmentRoot, path)) {
								foundRoots.add(packageFragmentRoot);
							}
						}

						// Stop after the first successful search
						if (!foundRoots.isEmpty()) {
							pckRoots.addAll(foundRoots);
							break;
						}
					}
				}

			}

			return pckRoots.toArray(new IPackageFragmentRoot[pckRoots.size()]);
		} else {
			return null;
		}
	}

	/**
	 * Attempts to resolve a main type in the associated Java project. Throws
	 * {@link CoreException} if it failed to resolve main type or no main type
	 * found.
	 *
	 * @param monitor
	 * @return non-null Main type.
	 * @throws CoreException
	 *             if failure occurred while resolving main type or no main type
	 *             found
	 */
	public IType getMainType(IProgressMonitor monitor) throws Exception {
		if (type == null) {
			type = new JavaTypeResolver(javaProject).getMainTypesFromSource(monitor);
			if (type == null) {
				throw ExceptionUtil.coreException(
						"No main type found. Verify that the project can be packaged as a jar application and contains a main type in source. War packaging of projects is not yet supported.");
			}
		}
		return type;
	}

	protected List<IJavaProject> getOrderedJavaProjects(IJavaProject project) {
		List<String> collectedProjects = new ArrayList<String>();
		getOrderedJavaProjectNames(Arrays.asList(project.getProject().getName()), collectedProjects);

		List<IJavaProject> projects = new ArrayList<IJavaProject>();

		for (String name : collectedProjects) {
			IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (prj != null) {
				IJavaProject jvPrj = JavaCore.create(prj);
				if (jvPrj != null && jvPrj.exists()) {
					projects.add(jvPrj);
				}
			}
		}

		return projects;

	}

	protected void getOrderedJavaProjectNames(List<String> sameLevelRequiredProjects, List<String> collectedProjects) {
		// The order in which required projects are collected is as follows,
		// with the RHS
		// being required projects of the LHS
		// A -> BC
		// B -> D
		// C -> E
		// = total 5 projects, added in the order that they are encountered.
		// so final ordered list should be ABCDE
		if (sameLevelRequiredProjects == null) {
			return;
		}
		List<String> nextLevelRequiredProjects = new ArrayList<String>();
		// First add the current level java projects in the order they appear
		// and also collect each one's required names.
		for (String name : sameLevelRequiredProjects) {
			try {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (project != null) {
					IJavaProject jvPrj = JavaCore.create(project);
					if (jvPrj != null && jvPrj.exists()) {
						if (!collectedProjects.contains(name)) {
							collectedProjects.add(name);
						}
						String[] names = jvPrj.getRequiredProjectNames();
						if (names != null && names.length > 0) {
							for (String reqName : names) {
								if (!nextLevelRequiredProjects.contains(reqName)) {
									nextLevelRequiredProjects.add(reqName);
								}
							}
						}
					}
				}

			} catch (JavaModelException e) {
				BootDashActivator.log(e);
			}

		}

		// Now recurse to fetch the required projects for the
		// list of java projects that were added at the current level above
		if (!nextLevelRequiredProjects.isEmpty()) {
			getOrderedJavaProjectNames(nextLevelRequiredProjects, collectedProjects);

		}
	}

	protected ILaunchConfiguration createConfiguration(IType type) throws Exception {

		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();

		ILaunchConfigurationType configType = manager
				.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

		ILaunchConfigurationWorkingCopy workingCopy = configType.newInstance(null,
				manager.generateLaunchConfigurationName(type.getTypeQualifiedName('.')));
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, type.getFullyQualifiedName());
		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				type.getJavaProject().getElementName());
		workingCopy.setMappedResources(new IResource[] { type.getUnderlyingResource() });
		return workingCopy;
	}

	protected IPath[] getRuntimeClasspaths(ILaunchConfiguration configuration) throws Exception {
		IRuntimeClasspathEntry[] entries = JavaRuntime.computeUnresolvedRuntimeClasspath(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);

		ArrayList<IPath> userEntries = new ArrayList<IPath>(entries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {

				String location = entries[i].getLocation();
				if (location != null) {
					IPath entry = Path.fromOSString(location);
					if (!userEntries.contains(entry)) {
						userEntries.add(entry);
					}
				}
			}
		}
		return userEntries.toArray(new IPath[userEntries.size()]);
	}

	/**
	 *
	 * Determines if the given package fragment root corresponds to the class
	 * path entry path.
	 * <p/>
	 * Note that different package fragment roots may point to the same class
	 * path entry.
	 * <p/>
	 * Example:
	 * <p/>
	 * A Java project may have the following package fragment roots:
	 * <p/>
	 * - src/main/java
	 * <p/>
	 * - src/main/resources
	 * <p/>
	 * Both may be using the same output folder:
	 * <p/>
	 * target/classes.
	 * <p/>
	 * In this case, the output folder will have a class path entry -
	 * target/classes - and it will be the same for both roots, and this method
	 * will return true for both roots if passed the entry for target/classes
	 *
	 * @param root
	 *            to check if it corresponds to the given class path entry path
	 * @param entry
	 * @return true if root is at the given entry
	 */
	private static boolean isRootAtEntry(IPackageFragmentRoot root, IPath entry) {
		try {
			IClasspathEntry cpe = root.getRawClasspathEntry();
			if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IPath outputLocation = cpe.getOutputLocation();
				if (outputLocation == null) {
					outputLocation = root.getJavaProject().getOutputLocation();
				}

				IPath location = ResourcesPlugin.getWorkspace().getRoot().findMember(outputLocation).getLocation();
				if (entry.equals(location)) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			BootDashActivator.log(e);
		}

		IResource resource = root.getResource();
		if (resource != null && entry.equals(resource.getLocation())) {
			return true;
		}

		IPath path = root.getPath();
		if (path != null && entry.equals(path)) {
			return true;
		}

		return false;
	}
}
