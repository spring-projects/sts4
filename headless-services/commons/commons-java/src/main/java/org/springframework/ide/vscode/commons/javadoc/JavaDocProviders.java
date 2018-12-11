/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
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

import org.springframework.ide.vscode.commons.java.IJavadocProvider;
import org.springframework.ide.vscode.commons.languageserver.java.ls.Classpath.CPE;

public class JavaDocProviders {

	public static IJavadocProvider createFor(CPE classpathEntry) {
		if (classpathEntry!=null && classpathEntry.getJavadocContainerUrl() != null) {
			URL containerUrl = classpathEntry.getJavadocContainerUrl();

			TypeUrlProviderFromContainerUrl urlProvider = isJarUrl(containerUrl)
				? TypeUrlProviderFromContainerUrl.JAR_JAVADOC_URL_PROVIDER
				: TypeUrlProviderFromContainerUrl.JAVADOC_FOLDER_URL_SUPPLIER;
			return new HtmlJavadocProvider(
					type -> urlProvider.url(classpathEntry.getJavadocContainerUrl(), type.getFullyQualifiedName(), type.classpathContainer().getModule())
			);
		}
		return null;
	}

	private static boolean isJarUrl(URL containerUrl) {
		return containerUrl.toString().endsWith(".jar");
	}

}
