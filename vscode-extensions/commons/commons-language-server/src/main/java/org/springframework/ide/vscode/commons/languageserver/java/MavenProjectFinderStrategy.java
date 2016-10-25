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

import org.springframework.ide.vscode.commons.languageserver.util.IDocument;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.FileUtils;
import org.springframework.ide.vscode.commons.util.StringUtil;

/**
 * Finds Maven Project based
 *
 * @author Alex Boyko
 *
 */
public class MavenProjectFinderStrategy implements IJavaProjectFinderStrategy {

	@Override
	public MavenJavaProject find(IDocument d) {
		String uriStr = d.getUri();
		if (StringUtil.hasText(uriStr)) {
			try {
				URI uri = new URI(uriStr);
				//TODO: This only work with File uri. Should it work with others too?
				File file = new File(uri).getAbsoluteFile();
				File pomFile = FileUtils.findFile(file, MavenCore.POM_XML);
				if (pomFile!=null) {
					return new MavenJavaProject(pomFile);
				}
			} catch (URISyntaxException | IllegalArgumentException e) {
				//garbage data. Ignore it.
			} catch (Exception e) {
				//TODO: Erroneous Pom file. Ignore it? Log?
			}
		}
		return null;
	}

}
