/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.javadoc;

import java.net.URL;
import java.nio.file.Paths;

@FunctionalInterface
public interface TypeUrlProviderFromContainerUrl {

	static String extractTopLevelType(String fqName) {
		int innerTypeIdx = fqName.indexOf('$');
		return innerTypeIdx > 0 ? fqName.substring(0, innerTypeIdx) : fqName;
	}

	public static final TypeUrlProviderFromContainerUrl JAR_SOURCE_URL_PROVIDER = (jarSourceUrl, fqName, module) -> {
		StringBuilder urlStr = new StringBuilder();
		urlStr.append("jar:");
		urlStr.append(jarSourceUrl);
		urlStr.append("!");
		urlStr.append('/');
		if (module != null) {
			urlStr.append(module);
			urlStr.append('/');
		}
		urlStr.append(extractTopLevelType(fqName).replaceAll("\\.", "/"));
		urlStr.append(".java");
		return new URL(urlStr.toString());

	};

	public static final TypeUrlProviderFromContainerUrl SOURCE_FOLDER_URL_SUPPLIER = (sourceContainerUrl, fqName, module) -> {
		// Unclear how to deal with modules and source folders at the moment hence module is ignored
		return Paths.get(sourceContainerUrl.toURI()).resolve(extractTopLevelType(fqName).replaceAll("\\.", "/") + ".java").toUri().toURL();
	};

	public static final TypeUrlProviderFromContainerUrl JAR_JAVADOC_URL_PROVIDER = (javadocContainerUrl, fqName, module) -> {
		StringBuilder urlStr = new StringBuilder();
		urlStr.append("jar:");
		urlStr.append(javadocContainerUrl);
		urlStr.append("!");
		urlStr.append('/');
		// Inner classes are in separate Top.Nesting1.Nesting2.Nesting3.MyType.html files
		urlStr.append(fqName.replaceAll("\\.", "/").replaceAll("\\$", "."));
		urlStr.append(".html");
		return new URL(urlStr.toString());

	};

	public static final TypeUrlProviderFromContainerUrl JAVADOC_FOLDER_URL_SUPPLIER = (javadocContainerUrl, fqName, module) -> {
		String urlStr = javadocContainerUrl.toString();
		StringBuilder sb = new StringBuilder(urlStr);
		if (!urlStr.endsWith("/")) {
			sb.append('/');
		}
		// Inner classes are in separate Top.Nesting1.Nesting2.Nesting3.MyType.html files
		sb.append(fqName.replaceAll("\\.", "/").replaceAll("\\$", ".") + ".html");
		return new URL(sb.toString());
	};

	URL url(URL containerUrl, String fqName, String module) throws Exception;

}
