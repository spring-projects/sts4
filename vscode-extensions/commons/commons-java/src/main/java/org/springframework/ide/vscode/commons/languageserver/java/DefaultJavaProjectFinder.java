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

package org.springframework.ide.vscode.commons.languageserver.java;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class DefaultJavaProjectFinder implements JavaProjectFinder {

	private final IJavaProjectFinderStrategy[] strategies;
	
	public DefaultJavaProjectFinder(IJavaProjectFinderStrategy[] strategies) {
		this.strategies = strategies;
	}

	@Override
	public IJavaProject find(IDocument d) {
		for (IJavaProjectFinderStrategy strategy : strategies) {
			try {
				IJavaProject project = strategy.find(d);
				if (project != null) {
					return project;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return null;
	}
	
}
