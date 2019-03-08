/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.languageserver.testharness;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.protocol.java.Classpath;
import org.springframework.ide.vscode.commons.protocol.java.Classpath.CPE;

public class ClasspathTestUtil {

	public static Path getOutputFolder(IJavaProject jp) throws Exception {
		for (CPE cpe : jp.getClasspath().getClasspathEntries()) {
			if (Classpath.isSource(cpe)) {
				if (cpe.getPath().endsWith("main/java")) {
					return Paths.get(cpe.getOutputFolder());
				}
			}
		}
		return null;
	}
}
