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

import java.util.List;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.validation.generations.ProjectVersionDiagnosticProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringIoProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectDiagnostic;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsClient;
import org.springframework.ide.vscode.boot.validation.generations.SpringProjectsProvider;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidators;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.stereotype.Component;

@Component
public class BootVersionValidator {
	
	private static final Logger log = LoggerFactory.getLogger(BootVersionValidator.class);

	
	public BootVersionValidator(SimpleLanguageServer server, ProjectObserver observer) {
		
		observer.addListener(new ProjectObserver.Listener() {
			
			@Override
			public void deleted(IJavaProject project) {
			}
			
			@Override
			public void created(IJavaProject project) {
				
				String url = getSpringProjectsUrl();
				SpringProjectsClient client = new SpringProjectsClient(url);
				SpringProjectsProvider provider = new SpringIoProjectsProvider(client);
				VersionValidators validators = new VersionValidators();
				ProjectVersionDiagnosticProvider diagnosticProvider = new ProjectVersionDiagnosticProvider(provider, validators);
				
				try {
					List<SpringProjectDiagnostic> diagnostics = diagnosticProvider.getDiagnostics(project);
					if (diagnostics != null) {
						for (SpringProjectDiagnostic springProjectDiagnostic : diagnostics) {
							server.getTextDocumentService().publishDiagnostics(new TextDocumentIdentifier(springProjectDiagnostic.getUri().toString()), List.of(springProjectDiagnostic.getDiagnostic()));
						}
					}
				} catch (Exception e) {
					log.error("Failed validating Spring Project version", e);
				}
				
			}
			
			@Override
			public void changed(IJavaProject project) {
				
			}
		});
	}
	

	private String getSpringProjectsUrl() {
		// TODO: Read from preferences
		return "https://spring.io/api/projects";
	}

}
