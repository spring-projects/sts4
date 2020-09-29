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
package org.springframework.ide.eclipse.boot.dash.cf.packaging;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * Responsible for picking the names for dependency jars when packaging a boot app.
 * <p>
 * This class could be turned into an interface with multiple implementations
 * should we want to support different naming strategies. But for now there's
 * just one implementation, so this is a concrete class.
 *
 * @author Kris De Volder
 */
public class JarNameGenerator {

	private static final String UNKNOWN_DEPENDENCY = "unknown-dependency";
	private static final IResource[] NO_RESOURCES = {};
	private Set<String> used = new HashSet<String>();

	private final String EXTENSION = ".jar";

	public String createName(File dep) {
		String candidate = getNameCandidate(dep);
		return makeUnique(candidate);
	}

	private String makeUnique(String candidate) {
		if (candidate.toLowerCase().endsWith(EXTENSION)) {
			candidate = candidate.substring(0, candidate.length()-EXTENSION.length());
		}
		String name = candidate + EXTENSION;
		int id = 0;
		while (used.contains(name)) {
			name = candidate+"-"+(++id) + EXTENSION;
		}
		used.add(name);
		return name;
	}

	protected String getNameCandidate(File dep) {
		if (isJar(dep)) {
			return getNameCandidateForJar(dep);
		} else if (dep.isDirectory()) {
			return getNameCandidateForDir(dep);
		}
		//Shouldn't happen, but return something anyway
		return UNKNOWN_DEPENDENCY;
	}

	private String getNameCandidateForDir(File dep) {
		//We'll try to use the name of enclosing workspace project if possible
		IProject p = getEnclosingProject(dep);
		if (p!=null) {
			return p.getName();
		}
		return UNKNOWN_DEPENDENCY;
	}

	protected IProject getEnclosingProject(File dep) {
		IResource best = chooseBest(getResources(dep));
		if (best!=null) {
			return best.getProject();
		}
		return null;
	}

	/**
	 * If more than one resource matched a dependency we try to find the
	 * 'best' resource. We do this by looking at the path relative to the workspace
	 * and pick the one with the shortest path. This way we will pick the resource
	 * in the most nested project (i.e. when projects are nested a resource
	 * may exists both in the parent and child projects. We are interested in
	 * the child in this case.
	 */
	protected IResource chooseBest(IResource[] resources) {
		int bestLen = Integer.MAX_VALUE;
		IResource best = null;
		for (IResource r : resources) {
			int len = r.getFullPath().segmentCount();
			if (len < bestLen) {
				best = r;
				bestLen = len;
			}
		}
		return best;
	}

	protected IResource[] getResources(File dep) {
		if (dep.isDirectory()) {
			return getWorkspaceRoot().findContainersForLocationURI(dep.toURI());
		} else if (dep.isFile()) {
			return getWorkspaceRoot().findFilesForLocationURI(dep.toURI());
		}
		return NO_RESOURCES;
	}

	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	protected String getNameCandidateForJar(File dep) {
		return dep.getName();
	}

	protected boolean isJar(File dep) {
		String name = dep.getName();
		return name.endsWith(".jar") || name.endsWith(".JAR");
	}

}
