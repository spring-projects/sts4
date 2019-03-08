/*******************************************************************************
 * Copyright (c) 2014 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata.util;

import java.util.Arrays;

import org.springframework.ide.vscode.commons.util.ListenerList;

public class ListenerManager<T> {

	private ListenerList<T> listeners = new ListenerList<>(ListenerList.IDENTITY);

	public void addListener(T l) {
		listeners.add(l);
	}

	public void removeListener(T l) {
		listeners.remove(l);
	}

	@SuppressWarnings("unchecked")
	public Iterable<T> getListeners() {
		return (Iterable<T>) Arrays.asList(listeners.getListeners());
	}

	
	
}
