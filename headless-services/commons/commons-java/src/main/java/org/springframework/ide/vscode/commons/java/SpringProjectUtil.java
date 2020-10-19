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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringProjectUtil {

	public static final Logger log = LoggerFactory.getLogger(SpringProjectUtil.class);
		
	private static final Pattern MAJOR_MINOR_VERSION = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)");

	// Pattern copied from https://semver.org/
	private static final Pattern VERSION = Pattern.compile("(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?");

	private static final Pattern SPRING_NAME = Pattern.compile("([a-z]+)(-[a-z]+)*");
	
	public static boolean isSpringProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-core", true);
	}

	public static boolean isBootProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot", true);
	}

	public static boolean hasBootActuators(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot-actuator-", true);
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
	
	public static String getVersion(String name) {
		Matcher matcher = VERSION.matcher(name);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			return name.substring(start, end);
		}
		return null;
	}
	
	/**
	 * 
	 * @param libName e.g. spring-boot-3.0.0.RELEASE.jar
	 * @return "slug" portion: "spring-boot"
	 */
	public static String getProjectSlug(String libName) {
		Matcher matcher = SPRING_NAME.matcher(libName);
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			return libName.substring(start, end);
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
	
	private static boolean hasSpecificLibraryOnClasspath(IJavaProject jp, String libraryNamePrefix, boolean onlyLibs) {
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

	private static boolean isEntry(File cpe, String libNamePrefix, boolean onlyLibs) {
		String name = cpe.getName();
		return name.startsWith(libNamePrefix) && (!onlyLibs || name.endsWith(".jar"));
	}
}
