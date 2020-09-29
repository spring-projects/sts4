/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cf.routes;

import org.eclipse.core.runtime.Assert;

/**
 * Represents an intention to set a value to either:
 * <ul>
 *   <li> a specific value, or
 *   <li> a randomly chosen value.
 * </ul>
 */
public class Randomized<T> {

	private final T fixedValue;

	private Randomized(T v) {
		this.fixedValue = v;
	}

	public boolean isRandom() {
		return this.fixedValue==null;
	}

	public T getValue() {
		Assert.isNotNull(this.fixedValue);
		return this.fixedValue;
	}

	public static <T> Randomized<T> value(T value) {
		Assert.isNotNull(value);
		return new Randomized<>(value);
	}

	public static <T> Randomized<T> random() {
		return new Randomized<>(null);
	}

	@Override
	public String toString() {
		if (isRandom()) {
			return "?";
		} else {
			return getValue().toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fixedValue == null) ? 0 : fixedValue.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Randomized other = (Randomized) obj;
		if (fixedValue == null) {
			if (other.fixedValue != null)
				return false;
		} else if (!fixedValue.equals(other.fixedValue))
			return false;
		return true;
	}

}
