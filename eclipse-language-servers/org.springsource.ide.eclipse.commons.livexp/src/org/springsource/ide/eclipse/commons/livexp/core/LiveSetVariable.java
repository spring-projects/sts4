/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springsource.ide.eclipse.commons.livexp.core.ObservableSet;

import com.google.common.collect.ImmutableSet;

/**
 * Similar to a 'live variable' but represents a set of values that can be listened
 * to. At the moment only coarse grained change events are produced. I.e. there
 * is just one event for "the Set has changed".
 * <p>
 * To allow more efficient incemental processing, clients may be interested in
 * just knowing about individual elements getting added / removed.
 * This is not yet supported.
 * <p>
 * Note: this class is meant to eventually replace LiveSet in commons which should
 * then become deprecated and, eventually, removed.
 */
public class LiveSetVariable<T> extends ObservableSet<T> {

	/**
	 * To be able to efficiently check that backing collection has changed.
	 * This assumes the backing collection is owned by the instance and it isn't
	 * mutated externally.
	 */
	private boolean dirty = false;
	private Set<T> backingCollection;

	public LiveSetVariable() {
		this(AsyncMode.SYNC);
	}
	
	/**
	 * Instantiate a LiveSet that uses a HashSet as a backing collection.
	 */
	public LiveSetVariable(AsyncMode async) {
		this(new HashSet<T>(), async);
	}

	public LiveSetVariable(Set<T> backingCollection) {
		this(backingCollection, AsyncMode.SYNC);
	}

	/**
	 * Instantiate a LiveSet with a specific backing collection. It is assumed that
	 * the backing collection henceforth is owned by the LiveSet. Client code should
	 * not retain references to the backing collection and should only modify the
	 * collection via liveset operations.
	 */
	public LiveSetVariable(Set<T> backingCollection, AsyncMode async) {
		super(ImmutableSet.copyOf(backingCollection), AsyncMode.SYNC, async);
		//Note the 'refresh' is done synchronously as its the most logical from user point of
		// view. Notably: otherwise when replacing values and immediately thereafter calling 'getValues'
		// you are not guaranteed to actually get the values you just inserted back.
		this.backingCollection = backingCollection;
	}

	@Override
	protected void syncRefresh() {
		//TODO: if we override 'refresh' as well we can avoid scheduling a job if not dirty.
		// Or alternately we can add isDirty() method to the protocol of AsyncLiveExpression
		// and buid this optimization into AsyncLiveExpression itself.

		//We override refresh method so we can avoid doing set comparison by making
		// use of a dirty flag instead.
		synchronized (this) {
			if (!dirty) return; //bail out fast and don't copy the collection needlessly
			value = compute();
		}
		//Note... we are being careful here to put the 'changed' call outside synch block.
		// only keep locks for short time while maniping the collection  / dirty state.
		// but notify listeners without holding on to the lock while listeneres are
		// doing their thing (which could be anything... and lead to deadlocks otherwise!)
		changed();
	}

	@Override
	protected synchronized ImmutableSet<T> compute() {
		return ImmutableSet.copyOf(backingCollection);
	}

	public void add(T name) {
		synchronized (this) {
			if (backingCollection.contains(name)) {
				//Nothing to do!
				return;
			} else {
				backingCollection.add(name);
				dirty = true;
			}
		}
		//Carefull... this leads to 'change' call, so must have released monitor before calling!
		refresh();
	}

	public void remove(T name) {
		synchronized (this) {
			if (!backingCollection.contains(name)) {
				//Nothing to do!
				return;
			} else {
				backingCollection.remove(name);
				dirty = true;
			}
		}
		refresh();
	}

	/**
	 * Batch-add a number of elements to the set. Only at most one change event will
	 * be fired no matter how many elements where actually added.
	 */
	public void addAll(T[] elements) {
		synchronized (this) {
			for (T e : elements) {
				dirty = backingCollection.add(e) || dirty;
			}
		}
		refresh();
	}

	public void addAll(Collection<T> elements) {
		synchronized (this) {
			for (T e : elements) {
				dirty = backingCollection.add(e) || dirty;
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
			Iterator<T> iter = backingCollection.iterator();
			while (iter.hasNext()) {
				T oldElement = iter.next();
				if (!newElements.contains(oldElement)) {
					iter.remove();
					dirty = true;
				}
			}

			//Add any missing new Elements
			for (T newElement : newElements) {
				dirty = backingCollection.add(newElement) || dirty;
			}
		}

		refresh(); //refreshes (if dirty)
	}

}
