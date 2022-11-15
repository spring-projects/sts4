/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringProjectUtil {

	public static final String SPRING_BOOT = "spring-boot";
	
	// Pattern copied from https://semver.org/
	private static final String VERSION_PATTERN_STR = "(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:(-|\\.)((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?";
	private static final String GENERATION_VERSION_STR = "([0-9]+)";

	public static final Logger log = LoggerFactory.getLogger(SpringProjectUtil.class);
		
	private static final Pattern GENERATION_VERSION = Pattern.compile(GENERATION_VERSION_STR);
	
	public static boolean isSpringProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-core", true);
	}

	public static boolean isBootProject(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, SPRING_BOOT, true);
	}

	public static boolean hasBootActuators(IJavaProject jp) {
		return hasSpecificLibraryOnClasspath(jp, "spring-boot-actuator-", true);
	}
	
	/**
	 *  Parses version from the given generation name (e.g. "2.1.x"
	 * @param name
	 * @return Version if valid generation name with major and minor components
	 * @throws Exception if invalid generation name
	 */
	public static Version getVersionFromGeneration(String name) throws Exception {
		Matcher matcher = GENERATION_VERSION.matcher(name);
		String major = null;
		String minor = null;
		
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			major =  name.substring(start, end);
		}
		
		if (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			minor =  name.substring(start, end);
		}
		
		if (major != null && minor != null) {
			return new Version(
					Integer.parseInt(major),
					Integer.parseInt(minor),
					0,
					null
			);
		}

		throw new IllegalArgumentException("Invalid semver. Unable to parse major and minor version from: " + name);
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
	
	public static Version getDependencyVersion(IJavaProject jp, String dependency) {		
		try {
			for (File f : IClasspathUtil.getBinaryRoots(jp.getClasspath(), (cpe) -> !cpe.isSystem())) {
				Version version = getDependencyVersion(f.getName(), dependency);
				if (version != null) {
					return version;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	public static Version getSpringBootVersion(IJavaProject jp) {		
		try {
			for (File f : IClasspathUtil.getBinaryRoots(jp.getClasspath(), (cpe) -> !cpe.isSystem())) {
				Version version = getDependencyVersion(f.getName(), SPRING_BOOT);
				if (version != null) {
					return version;
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	public static Version getDependencyVersion(String fileName, String dependency) {
		if (fileName.startsWith(dependency)) {
			StringBuilder sb = new StringBuilder();
			sb.append('^');
			sb.append(dependency);
			sb.append('-');
			sb.append(VERSION_PATTERN_STR);
			sb.append("\\.jar$");
			Pattern pattern = Pattern.compile(sb.toString());

			Matcher matcher = pattern.matcher(fileName);
			if (matcher.find() && matcher.groupCount() > 5) {
				String major = matcher.group(1);
				String minor = matcher.group(2);
				String patch = matcher.group(3);
				String qualifier = matcher.group(5);
				return new Version(
						Integer.parseInt(major),
						Integer.parseInt(minor),
						Integer.parseInt(patch),
						qualifier
				);
			}
		}
		return null;
	}
	
	public static Predicate<IJavaProject> springBootVersionGreaterOrEqual(int major, int minor, int patch) {
		return project -> {
			Version version = getDependencyVersion(project, SPRING_BOOT);
			if (version == null) {
				return false;
			}
			if (major > version.getMajor()) {
				return false;
			}
			if (major == version.getMajor()) {
				if (minor > version.getMinor()) {
					return false;
				}
				if (minor == version.getMinor()) {
					return patch <= version.getPatch();
				}
			}
			return true;
		};
	}

	public static Version getVersion(String version) {
		Pattern pattern = Pattern.compile(VERSION_PATTERN_STR);
		Matcher matcher = pattern.matcher(version);
		if (matcher.find() && matcher.groupCount() > 4) {
			String major = matcher.group(1);
			String minor = matcher.group(2);
			String patch = matcher.group(3);
			String qualifier = matcher.group(5);
			return new Version(
					Integer.parseInt(major),
					Integer.parseInt(minor),
					Integer.parseInt(patch),
					qualifier
			);
		}
		return null;
	}
}
