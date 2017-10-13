/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven.java;

import java.io.File;

import org.springframework.ide.vscode.commons.languageserver.java.AbstractFileToProjectCache;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Cache for Maven projects
 *
 * @author Alex Boyko
 */
public class MavenProjectCache extends AbstractFileToProjectCache<MavenJavaProject> {

	private MavenCore maven;
	
	public MavenProjectCache(FileObserver fileObserver, MavenCore maven) {
		super(fileObserver);
		this.maven = maven;
	}

	@Override
	protected void update(MavenJavaProject project) {
		project.update(maven);
	}

	@Override
	protected MavenJavaProject createProject(File pomFile) throws Exception {
		return new MavenJavaProject(maven, pomFile);
	}

}
