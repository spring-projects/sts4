/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.net.URI;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaModuleData;
import org.springframework.ide.vscode.commons.java.IJavaProject;

public class JdtJavaDocumentUriProvider implements JavaDocumentUriProvider {

	private static final Logger log = LoggerFactory.getLogger(JdtJavaDocumentUriProvider.class);

	@Override
	public URI docUri(IJavaProject project, String fqName) {
		return uri(project, fqName);
	}

	public static URI uri(IJavaProject project, String fqName) {
		IJavaModuleData classpathResource = project.getIndex().findClasspathResourceContainer(fqName);
		if (classpathResource != null) {
			File file = classpathResource.getContainer();
			if (file.isDirectory()) {
				IClasspath classpath = project.getClasspath();
				return SourceLinks.sourceFromSourceFolder(fqName, classpath).map(path -> path.toUri()).orElse(null);
			} else {
				try {
					int lastDotIndex = fqName.lastIndexOf('.');
					String packageName = fqName.substring(0, lastDotIndex);
					String typeName = fqName.substring(lastDotIndex + 1);
					String jarFileName = file.getName();
					StringBuilder sb = new StringBuilder();
					sb.append("jdt://contents/");
					sb.append(jarFileName);
					sb.append("/");
					sb.append(packageName);
					sb.append("/");
					sb.append(typeName);
					sb.append(SourceLinks.CLASS);
					sb.append("?");

					StringBuilder query = new StringBuilder();
					query.append("=");
					query.append(project.getElementName());
					query.append("/");
					String convertedPath = file.toString().replace(File.separator, "\\/");
					query.append(convertedPath);
					query.append("<");
					query.append(packageName);
					query.append("(");
					query.append(typeName);
					query.append(SourceLinks.CLASS);

					sb.append(URLEncoder.encode(query.toString(), "UTF8"));

					return URI.create(sb.toString());
				} catch (Throwable t) {
					log.warn("Failed creating Java document URI for " + file + " type " + fqName + " in the context of project " + project.getElementName(), t);
				}
			}
		}
		return null;
	}

}
