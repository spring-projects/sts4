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
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.FileObserver.FileListener;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.ListenerList;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Base implementation of {@link JavaProjectManager}
 * 
 * @author Alex Boyko
 *
 */
public abstract class AbstractJavaProjectManager implements JavaProjectManager {
	
	private ListenerList<Listener> listeners;
	
	private FileObserver fileObserver;
	
	private Supplier<FileListener> fileListener;
	
	public AbstractJavaProjectManager() {
		this.fileListener = Suppliers.memoize(() -> createFileListener());
		this.listeners = new ListenerList<>();
	}
	
	protected FileListener createFileListener() {
		return null;
	}

	@Override
	public IJavaProject find(IDocument doc) {
		try {
			String uriStr = doc.getUri();
			if (StringUtil.hasText(uriStr)) {
				URI uri = new URI(uriStr);
				// TODO: This only work with File uri. Should it work with others
				// too?
				if (uri.getScheme().equalsIgnoreCase("file")) {
					File file = new File(uri).getAbsoluteFile();
					return find(file);
				}
			}
		}
		catch (URISyntaxException e) {
			Log.log(e);
		}
		return null;
	}
	
	@Override
	public void setFileObserver(FileObserver fileObserver) {
		FileListener listener= fileListener.get();
		if (this.fileObserver != null && listener != null) {
			this.fileObserver.removeListener(listener);
		}
		this.fileObserver = fileObserver;
		if (this.fileObserver != null && listener != null) {
			this.fileObserver.addListener(listener);
		}
	}
	
	final protected FileObserver getFileObserver() {
		return this.fileObserver;
	}

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}
	
	final protected void notifyProjectCreated(IJavaProject project) {
		listeners.forEach(l -> l.created(project));
	}
	
	final protected void notifyProjectChanged(IJavaProject project) {
		listeners.forEach(l -> l.changed(project));
	}
	
	final protected void notifyProjectDeleted(IJavaProject project) {
		listeners.forEach(l -> l.deleted(project));
	}

}
