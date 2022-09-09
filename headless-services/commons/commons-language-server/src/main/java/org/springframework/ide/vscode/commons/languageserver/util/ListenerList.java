/*******************************************************************************
 * Copyright (c) 2016, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenerList<T> {
	
	private static Logger log = LoggerFactory.getLogger(ListenerList.class);
	
	private List<Consumer<T>> listeners = new ArrayList<>();

	public synchronized void fire(T evt) {
		for (Consumer<T> l : listeners) {
			try {
				l.accept(evt);
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	public void add(Consumer<T> l) {
		listeners.add(l);
	}

}
