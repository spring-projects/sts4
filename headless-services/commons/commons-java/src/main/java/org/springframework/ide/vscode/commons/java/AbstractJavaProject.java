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
package org.springframework.ide.vscode.commons.java;

import java.net.URI;
import java.nio.file.Path;

import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Abstract java project. Has a folder to store some project calculated data to speed up access
 *
 * @author Alex Boyko
 *
 */
public abstract class AbstractJavaProject extends JavaProject {

	final protected Path projectDataCache;

	public AbstractJavaProject(FileObserver fileObserver, URI loactionUri, Path projectDataCache, IClasspath classpath) {
		super(fileObserver, loactionUri, classpath);
		this.projectDataCache = projectDataCache;
	}

}
