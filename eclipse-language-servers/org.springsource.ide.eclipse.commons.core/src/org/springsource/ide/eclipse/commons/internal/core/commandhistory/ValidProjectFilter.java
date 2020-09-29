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
package org.springsource.ide.eclipse.commons.internal.core.commandhistory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springsource.ide.eclipse.commons.core.Entry;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;


/**
 * Utility class, provides a method to decide whether a given command history
 * Entry belongs to a project with the given nature.
 * <p>
 * The CommandHistory stores history items that may no longer be valid (because
 * the associated project has been closed/renamed/deleted etc.
 * <p>
 * A ValidProjectFilter can be applied to an Iterable<Entry> (which includes
 * CommandHistory) to only yield valid entries according to the current state of
 * the workspace.
 * <p>
 * The Filtering is efficient in the sense that it does not create an extra
 * collection containing the valid elements but only an iterator that skips over
 * invalid entries in the original collection.
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @author Kris De Volder
 * @since 2.5.0
 */
public class ValidProjectFilter implements Iterable<Entry> {

	private final Iterable<Entry> target;

	private final String natureId;

	public ValidProjectFilter(Iterable<Entry> target, String natureId) {
		this.target = target;
		this.natureId = natureId;
	}

	@SuppressWarnings("unchecked")
	public Iterator<Entry> iterator() {
		final Set<String> validProjects = new HashSet<String>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject project : projects) {
			// Test if the selected project has the given nature
			if (project.isAccessible() && SpringCoreUtils.hasNature(project, natureId)) {
				validProjects.add(project.getName());
			}
		}
		return new FilterIterator(target.iterator(), new Predicate() {
			public boolean evaluate(Object _entry) {
				Entry entry = (Entry) _entry;
				return validProjects.contains(entry.getProject());
			}
		});
	}

}
