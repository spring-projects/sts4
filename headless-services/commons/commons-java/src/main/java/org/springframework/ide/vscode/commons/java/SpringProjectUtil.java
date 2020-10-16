/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.java;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringProjectUtil {

	public static final Logger log = LoggerFactory.getLogger(SpringProjectUtil.class);
	
	private static final Pattern MAJOR_MINOR_VERSION = Pattern.compile("(\\d+\\.)(\\d+)");

	public static boolean isSpringProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-core", true);
	}

	public static boolean isBootProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot", true);
	}

	public static boolean hasBootActuators(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot-actuator-", true);
	}
	
	public static boolean hasSpecificLibraryOnClasspath(IJavaProject jp, String libraryNamePrefix, boolean onlyLibs) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				return IClasspathUtil.getBinaryRoots(cp, (cpe) -> !cpe.isSystem()).stream().anyMatch(cpe -> isEntry(cpe, libraryNamePrefix, onlyLibs));
			}
		} catch (Exception e) {
			log.error("Failed to determine whether '" + jp.getElementName() + "' is Spring Boot project", e);
		}
		return false;
	}

	public static File getLibraryOnClasspath(IJavaProject jp, String libraryNamePrefix) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				boolean onlyLibs = true;
				Optional<File> found = IClasspathUtil.getBinaryRoots(cp, (cpe) -> !cpe.isSystem()).stream().filter(cpe -> isEntry(cpe, libraryNamePrefix, onlyLibs)).findFirst();
				if (found.isPresent()) {
					return found.get();
				}
			}
		} catch (Exception e) {
			log.error("Failed to get library for project '" + jp.getElementName() + "' that start with prefix: " + libraryNamePrefix, e);
		}
		return null;
	}
	
	public static String getMajMinVersion(IJavaProject jp, String libraryNamePrefix) {
		if (jp != null) {
			File libraryOnClasspath = getLibraryOnClasspath(jp, libraryNamePrefix);
			if (libraryOnClasspath != null) {
				String name = libraryOnClasspath.getName();
				return getMajMinVersion(name);
			}
		}
		return null;
	}

	public static String getMajMinVersion(String name) {
		Matcher matcher = MAJOR_MINOR_VERSION.matcher(name);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			return name.substring(start, end);
		}
		return null;
	}

	public static List<File> getLibrariesOnClasspath(IJavaProject jp, String libraryNamePrefix) {
		try {
			IClasspath cp = jp.getClasspath();
			if (cp!=null) {
				boolean onlyLibs = true;
				List<File> libs = IClasspathUtil.getBinaryRoots(cp, (cpe) -> !cpe.isSystem()).stream().filter(cpe -> isEntry(cpe, libraryNamePrefix, onlyLibs)).collect(Collectors.toList());
				return libs;
			}
		} catch (Exception e) {
			log.error("Failed to get list of libraries for project '" + jp.getElementName() + "' that start with prefix: " + libraryNamePrefix, e);
		}
		return null;
	}

	private static boolean isEntry(File cpe, String libNamePrefix, boolean onlyLibs) {
		String name = cpe.getName();
		return name.startsWith(libNamePrefix) && (!onlyLibs || name.endsWith(".jar"));
	}
}
