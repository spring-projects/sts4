/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.gradle;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.springframework.ide.vscode.commons.languageserver.Sts4LanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.AbstractFileToProjectCache;
import org.springframework.ide.vscode.commons.languageserver.java.JavadocService;
import org.springframework.ide.vscode.commons.languageserver.util.ShowMessageException;

/**
 * Tests whether document belongs to a Gradle project
 *
 * @author Alex Boyko
 *
 */
public class GradleProjectCache extends AbstractFileToProjectCache<GradleJavaProject> {

	private GradleCore gradle;
	private JavadocService javadocService;

	public GradleProjectCache(Sts4LanguageServer server, GradleCore gradle, boolean asyncUpdate, Path projectCacheFolder, JavadocService javadocService) {
		super(server, asyncUpdate, projectCacheFolder);
		this.gradle = gradle;
		this.javadocService = javadocService;
	}

	@Override
	protected boolean update(GradleJavaProject project) {
		try {
			return project.update();
		} catch (Exception e) {
			server.getDiagnosticService().diagnosticEvent(new ShowMessageException(
					new MessageParams(MessageType.Error, "Cannot load Gradle project model from folder: " + project.getLocationUri()), e));
			return true;
		}
	}

	@Override
	protected GradleJavaProject createProject(File gradleBuild) throws Exception {
		File gradleFile = gradleBuild.getParentFile();
		GradleJavaProject gradleJavaProject = GradleJavaProject.create(getFileObserver(), gradle, gradleFile,
				projectCacheFolder == null ? null : gradleFile.toPath().resolve(projectCacheFolder),
				javadocService
			);
		performUpdate(gradleJavaProject, asyncUpdate, asyncUpdate);
		return gradleJavaProject;
	}

}
