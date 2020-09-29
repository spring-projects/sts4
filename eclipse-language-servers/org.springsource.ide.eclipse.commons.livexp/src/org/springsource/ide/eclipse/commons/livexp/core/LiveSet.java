/*******************************************************************************
 * Copyright (c) 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Similar to a 'live variable' but represents a set of values that can be listened
 * to. At the moment only coarse grained change events are produced. I.e. there
 * is just one event for "the Set has changed".
 * <p>
 * To allow more efficient incemental processing, clients may be interested in
 * just knowing about individual elements getting added / removed.
 * This is not yet supported.
 * 
 * @deprecated Use either {@link LiveSetVariable} (read+write) or {@link ObservableSet} (read-only)*/
public class LiveSet<T> extends LiveExpression<Set<T>> {

	/**
	 * To be able to efficiently check that backing collection has changed.
	 * This assumes the backing collection is owned by the instance and it isn't
	 * mutated externally.
	 */
	private boolean dirty = false;

	public LiveSet() {
		this(new HashSet<T>());
	}

	/**
	 * Instantiate a LiveSet with a specific backing collection. It is assumed that
	 * the backing collection henceforth is owned by the LiveSet. Clien code should
	 * not retain references to the backing collection and should only modify the
	 * collection via liveset operations.
	 */
	public LiveSet(Set<T> intialBackingCollection) {
		this.value = intialBackingCollection;
	}

	@Override
	public void refresh() {
		boolean wasDirty;
		synchronized (this) {
			wasDirty = dirty;
			dirty = false;
		}
		//Note... we are being careful here to put the 'changed' call outside synch block.
		// only keep locks for short time while maniping the collection  / dirty state.
		// but notify listeners without holding on to the lock while listeneres are
		// doing their thing (which could be anything... and lead to deadlocks otherwise!)
		if (wasDirty) {
			changed();
		}
	}

	@Override
	protected Set<T> compute() {
		throw new Error("Shouldn't be reachable because refresh is overridden");
	}

	public boolean contains(T name) {
		//TODO: it would be logical if contains actually returned a LiveExp<Boolean>. But we don't need it yet... and implementing this
		// correctly and efficiently is probably quite tricky.
		return value.contains(name);
	}

	public void add(T name) {
		synchronized (this) {
			if (value.contains(name)) {
				//Nothing to do!
				return;
			} else {
				value.add(name);
				dirty = true;
			}
		}
		//Carefull... this leads to 'change' call, so must have released monitor before calling!
		refresh();
	}

	public void remove(T name) {
		synchronized (this) {
			if (!value.contains(name)) {
				//Nothing to do!
				return;
			} else {
				value.remove(name);
				dirty = true;
			}
		}
		refresh();
	}

	/**
	 * Gets the current elements in the set as a list. The returned collection
	 * is a copy of the backing collection. So it is safe to work with this
	 * collection while other threads continue to make changes to the
	 * liveset.
	 */
	public synchronized List<T> getValues() {
		List<T> l = new ArrayList<T>(value.size());
		l.addAll(value);
		return l;
	}

	/**
	 * Batch-add a number of elements to the set. Only at most one change event will
	 * be fired no matter how many elements where actually added.
	 */
	public void addAll(T[] elements) {
		synchronized (this) {
			for (T e : elements) {
				dirty = value.add(e) || dirty;
			}
		}
		refresh();
	}

	public void addAll(Collection<T> elements) {
		synchronized (this) {
			for (T e : elements) {
				dirty = value.add(e) || dirty;
			}
		}
		refresh();
	}

	/**
	 * Replaces current elements with newElements. This is more efficient than
	 * using individual add/remove calls as it only generates at most one 'changed'
	 * event at the end of applying all the changes (if any).
	 */
	public void replaceAll(Collection<T> newElements) {
		synchronized (this) {
			//Remove any old elements that should no longer be there
			Iterator<T> iter = value.iterator();
			while (iter.hasNext()) {
				T oldElement = iter.next();
				if (!newElements.contains(oldElement)) {
					iter.remove();
					dirty = true;
				}
			}

			//Add any missing new Elements
			for (T newElement : newElements) {
				dirty = value.add(newElement) || dirty;
			}
		}

		refresh(); //refreshes (if dirty)
	}

}
