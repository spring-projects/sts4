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
package org.springframework.ide.eclipse.boot.dash.util;

import java.util.Comparator;

/**
 * Comparator that knows how to compare finite number of objects, based on
 * a desired sorting order that is provided to it in the constructor.
 * <p>
 * Asking this comparator to compare other objects than those provided
 * in the constructor will raise an IllegalArgumentException.
 *
 * @author Kris De Volder
 */
public class OrderBasedComparator<T> implements Comparator<T> {

	private T[] sortedElements;

	public OrderBasedComparator(T... sortedElements) {
		this.sortedElements = sortedElements;
	}

	@Override
	public int compare(T t1, T t2) {
		return getIndex(t1) - getIndex(t2);
	}

	private int getIndex(T t) {
		for (int i = 0; i < sortedElements.length; i++) {
			if (sortedElements[i].equals(t)) {
				return i;
			}
		}
		throw new IllegalArgumentException("This comparator doesn't know how to compare with "+t);
	}

}
