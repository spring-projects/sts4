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
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.java.IClasspath;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath;
import org.springframework.ide.vscode.commons.languageserver.jdt.ls.Classpath.CPE;
import org.springframework.ide.vscode.commons.util.CollectorUtil;

/**
 * @author Martin Lippert
 */
public class DefaultRunningAppProvider implements ProjectAwareRunningAppProvider {

	private RunningAppProvider allApps;

	public DefaultRunningAppProvider(RunningAppProvider allApps) {
		this.allApps = allApps;
	}

	private boolean strictProjectMatchingEnabled = false;

	public void setStrictProjectMatching(boolean strictProjectMatchingEnabled) {
		this.strictProjectMatchingEnabled = strictProjectMatchingEnabled;
	}

	@Override
	public Collection<SpringBootApp> getAllRunningSpringApps(IJavaProject project) throws Exception {
		Collection<SpringBootApp> apps = allApps.getAllRunningSpringApps();

		if (project != null && strictProjectMatchingEnabled) {
			return apps.stream().filter((app) -> {
				return doesProjectMatch(app, project);
			}).collect(CollectorUtil.toImmutableList());
		}
		else {
			return apps;
		}
	}

	private boolean doesProjectMatch(SpringBootApp app, IJavaProject project) {
		if (doesProjectNameMatch(app, project)) return true;
		if (doesProjectThinJarWrapperMatch(app, project)) return true;
		if (doesClasspathMatch(app, project)) return true;

		return false;
	}

	public static boolean doesClasspathMatch(SpringBootApp app, IJavaProject project) {
		try {
			Set<String> runningAppClasspath = new HashSet<>();
			Collections.addAll(runningAppClasspath, app.getClasspath());

			return doesClasspathMatch(runningAppClasspath, project);
		}
		catch (Exception e) {
			return false;
		}
	}

	public static boolean doesClasspathMatch(Set<String> runningAppClasspath, IJavaProject project) throws Exception {
		IClasspath classpath = project.getClasspath();
		Collection<CPE> entries = classpath.getClasspathEntries();
		for (CPE cpe : entries) {
			if (Classpath.ENTRY_KIND_SOURCE.equals(cpe.getKind())) {
				String path = cpe.getOutputFolder();
				if (runningAppClasspath.contains(path)) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean doesProjectThinJarWrapperMatch(SpringBootApp app, IJavaProject project) {
		return false;
	}

	public static boolean doesProjectNameMatch(SpringBootApp app, IJavaProject project) {
		try {
			String projectName = app.getSystemProperties("spring.boot.project.name");
			return projectName != null && projectName.equals(project.getElementName());
		}
		catch (Exception e) {
			return false;
		}
	}

}
