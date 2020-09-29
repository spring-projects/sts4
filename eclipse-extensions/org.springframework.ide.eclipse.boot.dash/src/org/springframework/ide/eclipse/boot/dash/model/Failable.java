/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;

public class Failable<T> {

	final private T value;
	final private HtmlSnippet error;

	public static <T> Failable<T> of(T value) {
		return new Failable<>(value, null);
	}

	public static <T> Failable<T> error(HtmlSnippet snippet) {
		return new Failable<>(null, snippet);
	}

	private Failable(T value, HtmlSnippet error) {
		Assert.isTrue((value != null && error == null) || (value == null && error != null));
		this.value = value;
		this.error = error;
	}

	@Override
	public String toString() {
		if (value != null) {
			return value.toString();
		} else {
			return error.toString();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((error == null) ? 0 : error.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Failable other = (Failable) obj;
		if (error == null) {
			if (other.error != null)
				return false;
		} else if (!error.equals(other.error))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	public boolean hasFailed() {
		return error != null;
	}

	public HtmlSnippet getErrorMessage() {
		return error;
	}

	public T getValue() {
		return this.value;
	}

	public T orElse(T obj) {
		return hasFailed() ? obj : value;
	}

}
