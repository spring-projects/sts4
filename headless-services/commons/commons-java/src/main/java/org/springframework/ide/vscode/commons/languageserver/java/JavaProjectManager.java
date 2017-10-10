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

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.FileObserver;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Java project manager. Able to find a java project for a file or document and
 * provide project change listening mechanism
 *
 * @author Alex Boyko
 */
public interface JavaProjectManager {
	
	interface Listener {
		void created(IJavaProject project);
		void changed(IJavaProject project);
		void deleted(IJavaProject project);
	}

	void setFileObserver(FileObserver fileObserver);
	IJavaProject find(IDocument doc);
	IJavaProject find(File file);
	boolean isProjectRoot(File file);
	
	void addListener(Listener listener);
	void removeListener( Listener listener);

}
