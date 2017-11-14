/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;

import org.springframework.ide.vscode.boot.java.handlers.RunningAppProvider;
import org.springframework.ide.vscode.boot.java.utils.BootProjectUtil;
import org.springframework.ide.vscode.boot.java.utils.SpringLiveHoverWatchdog;
import org.springframework.ide.vscode.boot.metadata.DefaultSpringPropertyIndexProvider;
import org.springframework.ide.vscode.boot.metadata.SpringPropertyIndexProvider;
import org.springframework.ide.vscode.commons.gradle.GradleCore;
import org.springframework.ide.vscode.commons.gradle.GradleProjectCache;
import org.springframework.ide.vscode.commons.gradle.GradleProjectFinder;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeProjectOvserver;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.LSFactory;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectCache;
import org.springframework.ide.vscode.commons.maven.java.MavenProjectFinder;
import org.springframework.ide.vscode.commons.util.FileObserver;

public class BootJavaLanguageServerParams {

	public final JavaProjectFinder projectFinder;
	public final ProjectObserver projectObserver;
	public final SpringPropertyIndexProvider indexProvider;
	public final RunningAppProvider runningAppProvider;
	public final Duration watchDogInterval;

	public BootJavaLanguageServerParams(
			JavaProjectFinder projectFinder,
			ProjectObserver projectObserver,
			SpringPropertyIndexProvider indexProvider,
			RunningAppProvider runningAppProvider,
			Duration watchDogInterval
	) {
		super();
		this.projectFinder = projectFinder;
		this.projectObserver = projectObserver;
		this.indexProvider = indexProvider;
		this.runningAppProvider = runningAppProvider;
		this.watchDogInterval = watchDogInterval;
	}

	public static LSFactory<BootJavaLanguageServerParams> createDefault() {
		return (SimpleLanguageServer server) -> {
			// Initialize project finders, project caches and project observers
			FileObserver fileObserver = server.getWorkspaceService().getFileObserver();
			CompositeJavaProjectFinder javaProjectFinder = new CompositeJavaProjectFinder();
			MavenProjectCache mavenProjectCache = new MavenProjectCache(fileObserver, MavenCore.getDefault(), true, Paths.get(IJavaProject.PROJECT_CACHE_FOLDER));
			javaProjectFinder.addJavaProjectFinder(new MavenProjectFinder(mavenProjectCache));

			GradleProjectCache gradleProjectCache = new GradleProjectCache(fileObserver, GradleCore.getDefault(), true, Paths.get(IJavaProject.PROJECT_CACHE_FOLDER));
			javaProjectFinder.addJavaProjectFinder(new GradleProjectFinder(gradleProjectCache));

			CompositeProjectOvserver projectObserver = new CompositeProjectOvserver(Arrays.asList(mavenProjectCache, gradleProjectCache));

			return new BootJavaLanguageServerParams(
					javaProjectFinder.filter(BootProjectUtil::isBootProject),
					projectObserver,
					new DefaultSpringPropertyIndexProvider(javaProjectFinder, projectObserver),
					RunningAppProvider.DEFAULT,
					SpringLiveHoverWatchdog.DEFAULT_INTERVAL
			);
		};
	}

	public static LSFactory<BootJavaLanguageServerParams> createTestDefault() {
		return (SimpleLanguageServer server) -> {
			// Initialize project finders, project caches and project observers
			FileObserver fileObserver = server.getWorkspaceService().getFileObserver();
			CompositeJavaProjectFinder javaProjectFinder = new CompositeJavaProjectFinder();
			MavenProjectCache mavenProjectCache = new MavenProjectCache(fileObserver, MavenCore.getDefault(), false, null);
			mavenProjectCache.setAlwaysFireEventOnFileChanged(true);
			javaProjectFinder.addJavaProjectFinder(new MavenProjectFinder(mavenProjectCache));

			GradleProjectCache gradleProjectCache = new GradleProjectCache(fileObserver, GradleCore.getDefault(), false, null);
			gradleProjectCache.setAlwaysFireEventOnFileChanged(true);
			javaProjectFinder.addJavaProjectFinder(new GradleProjectFinder(gradleProjectCache));

			CompositeProjectOvserver projectObserver = new CompositeProjectOvserver(Arrays.asList(mavenProjectCache, gradleProjectCache));

			return new BootJavaLanguageServerParams(
					javaProjectFinder.filter(BootProjectUtil::isBootProject),
					projectObserver,
					new DefaultSpringPropertyIndexProvider(javaProjectFinder, projectObserver),
					RunningAppProvider.DEFAULT,
					SpringLiveHoverWatchdog.DEFAULT_INTERVAL
			);
		};
	}

}
