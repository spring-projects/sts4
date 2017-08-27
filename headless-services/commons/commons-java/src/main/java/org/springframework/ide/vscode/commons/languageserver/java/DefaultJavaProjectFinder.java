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
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class DefaultJavaProjectFinder implements JavaProjectFinder {

	private final IJavaProjectFinderStrategy[] strategies;
	
	public DefaultJavaProjectFinder(IJavaProjectFinderStrategy[] strategies) {
		this.strategies = strategies;
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
	public IJavaProject find(File file) {
		for (IJavaProjectFinderStrategy strategy : strategies) {
			try {
				IJavaProject project = strategy.find(file);
				if (project != null) {
					return project;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return null;
	}

	@Override
	public boolean isProjectRoot(File file) {
		for (IJavaProjectFinderStrategy strategy : strategies) {
			try {
				if (strategy.isProjectRoot(file)) {
					return true;
				}
			} catch (Exception e) {
				Log.log(e);
			}
		}
		return false;
	}
	
}
