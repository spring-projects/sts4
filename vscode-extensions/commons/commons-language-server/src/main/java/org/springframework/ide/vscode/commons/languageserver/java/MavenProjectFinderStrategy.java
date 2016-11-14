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
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.StringUtil;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * Finds Maven Project based
 *
 * @author Alex Boyko
 *
 */
public class MavenProjectFinderStrategy implements IJavaProjectFinderStrategy {

	public Cache<File, MavenJavaProject> cache = CacheBuilder.newBuilder().build();

	@Override
	public MavenJavaProject find(IDocument d) throws ExecutionException, URISyntaxException {
		String uriStr = d.getUri();
		if (StringUtil.hasText(uriStr)) {
			URI uri = new URI(uriStr);
			// TODO: This only work with File uri. Should it work with others
			// too?
			if (uri.getScheme().equalsIgnoreCase("file")) {
				File file = new File(uri).getAbsoluteFile();
				File pomFile = FileUtils.findFile(file, MavenCore.POM_XML);
				if (pomFile != null) {
					return cache.get(pomFile, () -> {
						return new MavenJavaProject(pomFile);
					});
				}
			}
		}
		return null;
	}

}
