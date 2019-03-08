/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.java;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Optional;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.util.StringUtil;

/**
 * File-based abstract implementation of JavaProjectFinder.
 * <p>
 * Note that implementations derived from this class have a built-in 
 * limitation that they only work for documents stored on disk.
 * 
 * @author Alex Boyko
 * @author Kris De Volder
 */
public abstract class FileBasedJavaProjectFinder implements JavaProjectFinder {

	@Override
	public final Optional<IJavaProject> find(TextDocumentIdentifier doc) {
		try {
			String uriStr = doc.getUri();
			if (StringUtil.hasText(uriStr)) {
				URI uri = new URI(uriStr);
				if (uri.getScheme().equalsIgnoreCase("file")) {
					File file = new File(uri).getAbsoluteFile();
					return find(file);
				} else if (uri.getScheme().equalsIgnoreCase("jdt")) {
					// Example of URI to be parsed (this is what we get from JDT):
					//  jdt://contents/spring-boot-autoconfigure-1.5.7.RELEASE.jar/org.springframework.boot.autoconfigure/SpringBootApplication.class?%3Ddemo%2F%5C%2FUsers%5C%2Fhomeuser%5C%2F.m2%5C%2Frepository%5C%2Forg%5C%2Fspringframework%5C%2Fboot%5C%2Fspring-boot-autoconfigure%5C%2F1.5.7.RELEASE%5C%2Fspring-boot-autoconfigure-1.5.7.RELEASE.jar%3Corg.springframework.boot.autoconfigure%28SpringBootApplication.class 
					String host = uri.getHost();
					if ("contents".equals(host)) {
						String query = uri.getQuery();
						try {
							String decoded = URLDecoder.decode(query, "UTF-8");
							String projectName = getProjectName(decoded);
							return findProjectByName(projectName);
						} catch (UnsupportedEncodingException e) {
							Log.log(e);
						}
					}
				}
			}
		}
		catch (URISyntaxException e) {
			Log.log(e);
		}
		return Optional.empty();
	}

	private String getProjectName(String decodedQuery) {
		// Example of decoded query to be parsed (this is what we get from JDT). Note that "demo" in the example below is the project name that we want to parse:
		// =demo/\/Users\/homeuser\/.m2\/repository\/org\/springframework\/boot\/spring-boot-autoconfigure\/1.5.9.RELEASE\/spring-boot-autoconfigure-1.5.9.RELEASE.jar<org.springframework.boot.autoconfigure(SpringBootApplication.class	
		if (decodedQuery != null) {
			String[] segs = decodedQuery.split("=");
			if (segs.length > 1) {
				String remaining = segs[1];
				segs=remaining.split("\\/");
				if (segs.length > 0) {
					return segs[0];
				}
			}
		}
		return null;
	}
	
	protected abstract Optional<IJavaProject> findProjectByName(String name);

	protected abstract Optional<IJavaProject> find(File file);

}
