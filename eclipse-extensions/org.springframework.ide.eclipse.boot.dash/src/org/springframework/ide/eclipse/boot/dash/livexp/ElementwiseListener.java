/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

/**
 * An 'adapter' class that can used to create a ValueListener on a LiveSet which provides methods
 * that get called when individual elements get added or removed to the set (whereas the
 * general value listener treats the set itself as a value),
 *
 * @author Kris De Volder
 */
public abstract class ElementwiseListener<T> implements ValueListener<ImmutableSet<T>> {

	private Set<T> lastState = Collections.emptySet();

	@Override
	public final void gotValue(LiveExpression<ImmutableSet<T>> exp, ImmutableSet<T> newState) {
		Set<T> removed;
		Set<T> added;
		synchronized (this) {
			//compute the 'diffs' inside synch block
			//newState = new LinkedHashSet<T>(newState); //use a 'snapshot' because the set may change.
			added = minus(newState, lastState);
			removed = minus(lastState, newState);
			lastState = newState;
		}

		//TODO: race conditions sending events if set is changed in multiple threads?

		//sending 'diff' events outside sync block.
		for (T t : removed) {
			removed(exp, t);
		}
		for (T t : added) {
			added(exp, t);
		}
	}

	protected abstract void added(LiveExpression<ImmutableSet<T>> exp, T e);
	protected abstract void removed(LiveExpression<ImmutableSet<T>> exp, T e);

	private Set<T> minus(Set<T> set, Set<T> subtract) {
		LinkedHashSet<T> diff = new LinkedHashSet<>(set);
		diff.removeAll(subtract);
		return diff;
	}

}
