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

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;

/**
 * Cache fo java projects. The key for the cache is a "project" specific file
 * 
 * @author Alex Boyko
 *
 * @param <P> java project sub-class
 */
public abstract class AbstractFileToProjectCache<P extends IJavaProject> extends AbstractJavaProjectCache<File, P> {
	
	private String changeSubscription;
	private String deleteSubscription;
	
	public AbstractFileToProjectCache(FileObserver fileObserver) {
		super(fileObserver);
	}

	@Override
	protected void attachListeners(File file, P project) {
		super.attachListeners(file, project);
		List<String> globPattern = Arrays.asList(file.toString());
		changeSubscription = getFileObserver().onFileChanged(globPattern, (uri) -> {
			update(project);
			notifyProjectChanged(project);
		});
		deleteSubscription = getFileObserver().onFileDeleted(globPattern, (uri) -> {
			cache.invalidate(file);
			notifyProjectDeleted(project);
			getFileObserver().unsubscribe(changeSubscription);
			getFileObserver().unsubscribe(deleteSubscription);
		});
	}
	
	abstract protected void update(P project);
	
}
