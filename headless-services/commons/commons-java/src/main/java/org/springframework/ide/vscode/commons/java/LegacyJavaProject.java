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

import org.springframework.ide.vscode.commons.javadoc.JavaDocProviders;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Legacy java project. Base implementation for projects calculating classpath
 * and other Java related data locally on this LS. Data calculation is
 * expensive, hence there is a folder to store some project calculated data to
 * speed up access
 *
 * @author Alex Boyko
 *
 */
public class LegacyJavaProject extends JavaProject {

	final protected Path projectDataCache;

	public LegacyJavaProject(FileObserver fileObserver, URI loactionUri, Path projectDataCache, IClasspath classpath) {
		super(fileObserver, loactionUri, classpath, classpathResource -> {
			CPE cpe = IClasspathUtil.findEntryForBinaryRoot(classpath, classpathResource);
			return JavaDocProviders.createFor(cpe);
		});
		this.projectDataCache = projectDataCache;
	}

}
