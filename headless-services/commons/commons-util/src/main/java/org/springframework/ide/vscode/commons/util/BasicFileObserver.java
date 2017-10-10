/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.util;

/**
 * Basic implementation of File Observer interface
 * 
 * @author Alex Boyko
 *
 */
public class BasicFileObserver implements FileObserver {
	
	private ListenerList<FileListener> listeners = new ListenerList<>(); 

	@Override
	public void addListener(FileListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(FileListener listener) {
		listeners.remove(listener);
	}
	
	public void notifyFileChanged(String uri) {
		listeners.forEach(l -> {
			try {
				if (l.accept(uri)) {
					l.changed(uri);
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		});
	}
	
	public void notifyFileCreated(String uri) {
		listeners.forEach(l -> {
			try {
				if (l.accept(uri)) {
					l.created(uri);
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		});
	}
	
	public void notifyFileDeleted(String uri) {
		listeners.forEach(l -> {
			try {
				if (l.accept(uri)) {
					l.deleted(uri);
				}
			} catch (Throwable t) {
				Log.log(t);
			}
		});
	}
	
}
