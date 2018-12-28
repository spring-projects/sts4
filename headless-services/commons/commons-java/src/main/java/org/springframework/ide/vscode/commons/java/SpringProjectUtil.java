/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringProjectUtil {

	public static final Logger log = LoggerFactory.getLogger(SpringProjectUtil.class);

	public static boolean isSpringProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-core");
	}

	public static boolean isBootProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot");
	}

	private static boolean hasSpecificLibraryOnClasspath(IJavaProject jp, String libraryNamePrefix) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				return IClasspathUtil.getBinaryRoots(cp, (cpe) -> !cpe.isSystem()).stream().anyMatch(cpe -> isEntry(cpe, libraryNamePrefix));
			}
		} catch (Exception e) {
			log.error("Failed to determine whether '" + jp.getElementName() + "' is Spring Boot project", e);
		}
		return false;
	}

	private static boolean isEntry(File cpe, String libNamePrefix) {
		String name = cpe.getName();
		return name.endsWith(".jar") && name.startsWith(libNamePrefix);
	}
}
