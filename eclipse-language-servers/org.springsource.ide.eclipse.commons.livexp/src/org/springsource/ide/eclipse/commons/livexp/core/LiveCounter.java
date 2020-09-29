/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.livexp.core;

/**
 * An integer valued {@link LiveVariable} with some useful methods to
 * atomically update the counter's value.
 *
 * @author Kris De Volder
 */
public class LiveCounter extends LiveVariable<Integer> {

	public LiveCounter(int initialValue) {
		super(initialValue);
	}

	public LiveCounter() {
		this(0);
	}

	/**
	 * Increment the counter's value by 1 and return the new value.
	 */
	public int increment() {
		return increment(1);
	}

	/**
	 * Increment the counter's value by 1 and return the new value.
	 */
	public int decrement() {
		return increment(-1);
	}

	/**
	 * Increment this counter's value by a given delta and return the new value.
	 */
	public synchronized int increment(int delta) {
		int newValue = this.getValue()+delta;
		this.setValue(newValue);
		return newValue;
	}
}
