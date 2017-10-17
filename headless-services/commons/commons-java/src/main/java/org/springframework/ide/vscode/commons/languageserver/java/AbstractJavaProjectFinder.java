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
import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Abstract implementation of Java project finder interface
 * 
 * @author Alex Boyko
 *
 */
public abstract class AbstractJavaProjectFinder implements JavaProjectFinder {

	@Override
	public final IJavaProject find(IDocument doc) {
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

}
