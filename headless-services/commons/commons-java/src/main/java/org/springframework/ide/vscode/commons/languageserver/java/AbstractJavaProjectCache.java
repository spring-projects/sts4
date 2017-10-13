/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.ListenerList;
import org.springframework.ide.vscode.commons.util.Log;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Abstract implementation of java project cache indexed by keys
 * 
 * @author Alex Boyko
 *
 * @param <K> key class
 * @param <P> project class
 */
public abstract class AbstractJavaProjectCache<K, P extends IJavaProject> implements JavaProjectCache<K, P> {
	
	private FileObserver fileObserver;

	private ListenerList<Listener> listeners = new ListenerList<>();

	protected Cache<K, P> cache = CacheBuilder.newBuilder().build();
	
	public AbstractJavaProjectCache(FileObserver fileObserver) {
		this.fileObserver = fileObserver;
	}

	@Override
	public P project(K key) {
		if (key != null) {
			try {
				return cache.get(key, () -> {
					try {
						P project = createProject(key);
						attachListeners(key, project);
						return project;
					} catch (Throwable t) {
						throw new ExecutionException(t);
					}
				});
			} catch (ExecutionException e) {
				Log.log(e);
				return null;
			}
		}
		return null;
	}
	
	abstract protected P createProject(K key) throws Exception;
	
	protected void attachListeners(K key, P project) {
		
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	final protected void notifyProjectCreated(P project) {
		listeners.forEach(l -> l.created(project));
	}
	
	final protected void notifyProjectChanged(P project) {
		listeners.forEach(l -> l.changed(project));
	}
	
	final protected void notifyProjectDeleted(P project) {
		listeners.forEach(l -> l.deleted(project));
	}

	final protected FileObserver getFileObserver() {
		return fileObserver;
	}
}
