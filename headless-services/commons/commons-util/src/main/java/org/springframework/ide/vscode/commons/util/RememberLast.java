/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.util;

/**
 * Requestor that remembers only the last item received.
 *
 * @author Kris De Volder
 */
public class RememberLast<T> implements IRequestor<T> {

	private T last = null;

	@Override
	public void accept(T node) {
		this.last = node;
	}

	/**
	 * @return the last received item, may return null if no items where received.
	 */
	public T get() {
		return last;
	}

}
