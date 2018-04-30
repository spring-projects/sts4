/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.net.URI;

import org.springframework.ide.vscode.commons.jandex.JandexClasspath;
import org.springframework.ide.vscode.commons.util.FileObserver;

import reactor.core.Disposable;

public class JavaProject implements IJavaProject, Disposable {

	private final IClasspath classpath;
	private ClasspathIndex index;
	private URI uri;
	private final FileObserver fileObserver;

	public JavaProject(FileObserver fileObserver, URI uri, IClasspath classpath) {
		super();
		this.classpath = classpath;
		this.fileObserver = fileObserver;
	}

	@Override
	public IClasspath getClasspath() {
		return classpath;
	}

	@Override
	public synchronized ClasspathIndex getIndex() {
		if (index==null) {
			index = new JandexClasspath(classpath, fileObserver);
		}
		return index;
	}

	public URI getLocationUri() {
		return uri;
	}

	@Override
	public void dispose() {
		Disposable toDispose = null;
		synchronized (this) {
			toDispose = index;
			index = null;
		}
		if (toDispose!=null) {
			toDispose.dispose();
		}
	}

	@Override
	public boolean exists() {
		return new File(uri).exists();
	}
}
