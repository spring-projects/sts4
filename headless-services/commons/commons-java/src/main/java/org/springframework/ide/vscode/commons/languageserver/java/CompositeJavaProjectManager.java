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
import java.util.Arrays;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Composite project manager that acts a single project manager but consissts of many project managers
 * 
 * @author Alex Boyko
 *
 */
public class CompositeJavaProjectManager implements JavaProjectManager {
	
	private final JavaProjectManager[] projectManagers;
	
	public CompositeJavaProjectManager(JavaProjectManager[] projectManagers) {
		this.projectManagers = projectManagers;
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
		for (JavaProjectManager strategy : projectManagers) {
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
		for (JavaProjectManager strategy : projectManagers) {
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

	public void setFileObserver(FileObserver fileObserver) {
		Arrays.stream(projectManagers).forEach(pm -> pm.setFileObserver(fileObserver));
	}

	@Override
	public void addListener(Listener listener) {
		Arrays.stream(projectManagers).forEach(pm -> pm.addListener(listener));
	}

	@Override
	public void removeListener(Listener listener) {
		Arrays.stream(projectManagers).forEach(pm -> pm.removeListener(listener));
	}

}
