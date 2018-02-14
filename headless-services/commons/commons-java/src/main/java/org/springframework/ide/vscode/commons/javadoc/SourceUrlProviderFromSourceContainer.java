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

package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;
import java.nio.file.Paths;

@FunctionalInterface
public interface SourceUrlProviderFromSourceContainer {
	
	static String extractTopLevelType(String fqName) {
		int innerTypeIdx = fqName.indexOf('$');
		return innerTypeIdx > 0 ? fqName.substring(0, innerTypeIdx) : fqName;
	}
	
	public static final SourceUrlProviderFromSourceContainer JAR_SOURCE_URL_PROVIDER = (sourceContainerUrl, fqName) -> {
		StringBuilder sourceUrlStr = new StringBuilder();
		sourceUrlStr.append("jar:");
		sourceUrlStr.append(sourceContainerUrl);
		sourceUrlStr.append("!");
		sourceUrlStr.append('/');
		sourceUrlStr.append(extractTopLevelType(fqName).replaceAll("\\.", "/"));
		sourceUrlStr.append(".java");
		return new URL(sourceUrlStr.toString());

	};
	
	public static final SourceUrlProviderFromSourceContainer SOURCE_FOLDER_URL_SUPPLIER = (sourceContainerUrl, fqName) -> {
		return Paths.get(sourceContainerUrl.toURI()).resolve(extractTopLevelType(fqName).replaceAll("\\.", "/") + ".java").toUri().toURL();
	};
	
	public static final SourceUrlProviderFromSourceContainer JAR_JAVADOC_URL_PROVIDER = (javadocContainerUrl, fqName) -> {
		StringBuilder sourceUrlStr = new StringBuilder();
		sourceUrlStr.append("jar:");
		sourceUrlStr.append(javadocContainerUrl);
		sourceUrlStr.append("!");
		sourceUrlStr.append('/');
		// Inner classes are in separate Top.Nesting1.Nesting2.Nesting3.MyType.html files
		sourceUrlStr.append(fqName.replaceAll("\\.", "/").replaceAll("\\$", "."));
		sourceUrlStr.append(".html");
		return new URL(sourceUrlStr.toString());

	};
	
	public static final SourceUrlProviderFromSourceContainer JAVADOC_FOLDER_URL_SUPPLIER = (sourceContainerUrl, fqName) -> {
		String urlStr = sourceContainerUrl.toString();
		StringBuilder sb = new StringBuilder(urlStr);
		if (!urlStr.endsWith("/")) {
			sb.append('/');
		}
		// Inner classes are in separate Top.Nesting1.Nesting2.Nesting3.MyType.html files
		sb.append(fqName.replaceAll("\\.", "/").replaceAll("\\$", ".") + ".html");
		return new URL(sb.toString());
	};
	
	URL sourceUrl(URL sourceContainerUrl, String fqName) throws Exception;

}
