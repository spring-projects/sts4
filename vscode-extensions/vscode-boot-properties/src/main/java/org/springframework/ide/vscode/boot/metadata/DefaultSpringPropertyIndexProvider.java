/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.metadata;

import org.springframework.ide.vscode.boot.metadata.util.FuzzyMap;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class DefaultSpringPropertyIndexProvider implements SpringPropertyIndexProvider {

	private JavaProjectFinder javaProjectFinder;
	private SpringPropertiesIndexManager indexManager = new SpringPropertiesIndexManager(ValueProviderRegistry.getDefault());
	
	private ProgressService progressService = (id, msg) -> { /*ignore*/ };
	private static int progressIdCt = 0;
	
	public DefaultSpringPropertyIndexProvider(JavaProjectFinder javaProjectFinder) {
		this.javaProjectFinder = javaProjectFinder;
	}
	
	@Override
	public FuzzyMap<PropertyInfo> getIndex(IDocument doc) {
		String progressId = getProgressId();
		progressService.progressEvent(progressId, "Indexing Spring Boot Properties...");
		try {
			IJavaProject jp = javaProjectFinder.find(doc);
			if (jp!=null) {
				return indexManager.get(jp);
			}
		} finally {
			progressService.progressEvent(progressId, null);
		}
		return null;
	}

	private static synchronized String getProgressId() {
		return DefaultSpringPropertyIndexProvider.class.getName()+ (progressIdCt++);
	}

	public void setProgressService(ProgressService progressService) {
		this.progressService = progressService;
	}

}
