/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringIoProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidators;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider.DiagnosticResult;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationPreferences;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.stereotype.Component;

@Component
public class BootVersionValidator {

	private static final Logger log = LoggerFactory.getLogger(BootVersionValidator.class);

	private SimpleLanguageServer server;
	private BootJavaConfig config;
	
	public BootVersionValidator(SimpleLanguageServer server, BootJavaConfig config) {
		this.server = server;
		this.config = config;
	}
	
	public void validate(IJavaProject project) {
		if (config.isBootVersionValidationEnabled()) {
			log.info("Validating Spring Boot version on project: " + project.getElementName());
			
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
							new TextDocumentIdentifier(result.getDocumentUri().toString()),
							result.getDiagnostics());

				}
			} catch (Exception e) {
				log.error("Failed validating Spring Project version", e);
			}
		}
	}

	private String getSpringProjectsUrl(VersionValidationPreferences preferences) {
		return preferences.getSpringProjectsUrl();
	}
}
