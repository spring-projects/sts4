/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation;

import java.util.Collections;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.common.IJavaProjectReconcileEngine;
import org.springframework.ide.vscode.boot.common.ProjectReconcileScheduler;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider.DiagnosticResult;
import org.springframework.ide.vscode.boot.validation.generations.SpringIoProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidators;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationPreferences;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class BootVersionValidationEngine implements IJavaProjectReconcileEngine {

	private static final Logger log = LoggerFactory.getLogger(BootVersionValidationEngine.class);

	private SimpleLanguageServer server;
	private BootJavaConfig config;
	private ProjectReconcileScheduler projectReconcileScheduler;
	
	public BootVersionValidationEngine(SimpleLanguageServer server, BootJavaConfig config, ProjectObserver projectObserver, JavaProjectFinder projectFinder) {
		this.server = server;
		this.config = config;
		this.projectReconcileScheduler = new ProjectReconcileScheduler(this, projectFinder) {

			@Override
			protected void init() {
				super.init();
				config.addListener(evt -> scheduleValidationForAllProjects());
				projectObserver.addListener(new ProjectObserver.Listener() {
					
					@Override
					public void deleted(IJavaProject project) {
						unscheduleValidation(project);
						clear(project, true);
					}
					
					@Override
					public void created(IJavaProject project) {
						scheduleValidation(project);
					}
					
					@Override
					public void changed(IJavaProject project) {
						scheduleValidation(project);
					}
				});
			}
			
		};
	}
	
	public void reconcile(IJavaProject project) {
		if (config.isBootVersionValidationEnabled()) {
			log.debug("validating Spring Boot version on project: " + project.getElementName());
			long start = System.currentTimeMillis();
			
			VersionValidationPreferences preferences = new VersionValidationPreferences();

			String url = getSpringProjectsUrl(preferences);
			SpringProjectsClient client = new SpringProjectsClient(url);
			SpringProjectsProvider provider = new SpringIoProjectsProvider(client);
			VersionValidators validators = new VersionValidators(server.getDiagnosticSeverityProvider(), provider);

			ProjectVersionDiagnosticProvider diagnosticProvider = new ProjectVersionDiagnosticProvider(validators);

			try {
				DiagnosticResult result = diagnosticProvider.getDiagnostics(project);
				if (result != null && !result.getDiagnostics().isEmpty()) {
					server.getTextDocumentService().publishDiagnostics(
							new TextDocumentIdentifier(result.getDocumentUri().toASCIIString()),
							result.getDiagnostics());

				}
			} catch (Exception e) {
				log.error("Failed validating Spring Project version", e);
			}
			
			long end = System.currentTimeMillis();
			log.info("validating Spring Boot version on project: " + project.getElementName() + " done in " + (end - start) + "ms");
		}
	}

	private String getSpringProjectsUrl(VersionValidationPreferences preferences) {
		return preferences.getSpringProjectsUrl();
	}

	@Override
	public void clear(IJavaProject project) {
		// Build file
		if (project.getProjectBuild() != null && project.getProjectBuild().getBuildFile() != null) {
			server.getTextDocumentService().publishDiagnostics(
					new TextDocumentIdentifier(project.getProjectBuild().getBuildFile().toASCIIString()),
					Collections.emptyList());
		}
	}

	@Override
	public ProjectReconcileScheduler getScheduler() {
		return projectReconcileScheduler;
	}
}
