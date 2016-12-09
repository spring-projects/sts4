/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.maven;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.java.IJavaProjectFinderStrategy;
import org.springframework.ide.vscode.commons.maven.java.classpathfile.JavaProjectWithClasspathFile;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.IDocument;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class JavaProjectWithClasspathFileFinderStrategy implements IJavaProjectFinderStrategy {

	public Cache<File, JavaProjectWithClasspathFile> cache = CacheBuilder.newBuilder().build();

	@Override
	public JavaProjectWithClasspathFile find(IDocument d) throws ExecutionException, URISyntaxException {
		String uriStr = d.getUri();
		if (StringUtil.hasText(uriStr)) {
			URI uri = new URI(uriStr);
			// TODO: This only work with File uri. Should it work with others
			// too?
			if (uri.getScheme().equalsIgnoreCase("file")) {
				File file = toFile(uri);
				File cpFile = FileUtils.findFile(file, MavenCore.CLASSPATH_TXT);
				if (cpFile != null) {
					return cache.get(cpFile, () -> {
						return new JavaProjectWithClasspathFile(cpFile);
					});
				}
			}
		}
		return null;
	}

	protected File toFile(URI uri) {
//		try {
			return new File(uri).getAbsoluteFile();
//		} catch (Exception e) {
//			Log.log("Ignored Uri '"+uri+"'. Not a file?", e);
//			return null;
//		}
	}

}
