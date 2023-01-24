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
import java.util.function.Function;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.common.IJavaProjectReconcileEngine;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider.DiagnosticResult;
import org.springframework.ide.vscode.boot.validation.generations.SpringIoProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidators;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationPreferences;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class BootVersionValidationEngine implements IJavaProjectReconcileEngine {

	private static final Logger log = LoggerFactory.getLogger(BootVersionValidationEngine.class);

	private SimpleLanguageServer server;
	private BootJavaConfig config;
	
	public BootVersionValidationEngine(SimpleLanguageServer server, BootJavaConfig config) {
		this.server = server;
		this.config = config;
	}
	
	public void reconcile(IJavaProject project, Function<TextDocument, IProblemCollector> problemCollectorFactory) {
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
}
