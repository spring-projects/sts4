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
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootProjectUtil {

	public static final Logger log = LoggerFactory.getLogger(BootProjectUtil.class);


	public static boolean isBootProject(IJavaProject jp) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				return IClasspathUtil.getBinaryRoots(cp, (cpe) -> !cpe.isSystem()).stream().anyMatch(cpe -> isBootEntry(cpe));
			}
		} catch (Exception e) {
			log.error("Failed to determine whether '" + jp.getElementName() + "' is Spring Boot project", e);
		}
		return false;
	}

	private static boolean isBootEntry(File cpe) {
		String name = cpe.getName();
		return name.endsWith(".jar") && name.startsWith("spring-boot");
	}

	public static Path javaHomeFromLibJar(Path libJar) {
		for (Path home = libJar; home.getParent() != null; home = home.getParent()) {
			if (Files.exists(home.resolve("release")) || Files.exists(home.resolve("release.txt"))) {
				return home;
			}
		}
		return null;
	}

	public static Path jreSources(Path libJar) {
		Path home = javaHomeFromLibJar(libJar);
		if (home != null) {
			Path sources = home.resolve("src.zip");
			if (Files.exists(sources)) {
				return sources;
			}
			sources = home.resolve("lib/src.zip");
			if (Files.exists(sources)) {
				return sources;
			}
		}
		return null;
	}

}
