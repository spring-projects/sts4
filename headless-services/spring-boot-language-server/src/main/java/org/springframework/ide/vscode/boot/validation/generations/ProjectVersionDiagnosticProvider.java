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
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.commons.Version;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.MessageService;
import org.springframework.ide.vscode.commons.languageserver.PercentageProgressTask;
import org.springframework.ide.vscode.commons.languageserver.ProgressService;

public class ProjectVersionDiagnosticProvider {
	
	private static final Logger log = LoggerFactory.getLogger(ProjectVersionDiagnosticProvider.class);

	private final List<VersionValidator> validators;

	private ProgressService progressService;

	private MessageService messageService;

	public ProjectVersionDiagnosticProvider(ProgressService progressService, MessageService messageService, List<VersionValidator> validators) {
		this.progressService = progressService;
		this.messageService = messageService;
		this.validators = validators;
	}


	public DiagnosticResult getDiagnostics(IJavaProject javaProject) throws Exception {

		URI buildFileUri = javaProject.getProjectBuild() == null ? null : javaProject.getProjectBuild().getBuildFile();
		if (buildFileUri == null) {
			throw new Exception("Unable to find build file in project while computing version validation for: " + javaProject.getElementName());
		}

		List<VersionValidator> applicableValidators = validators.stream().filter(v -> v.isEnabled()).collect(Collectors.toList());
		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		
		if (!applicableValidators.isEmpty()) {
			PercentageProgressTask progress = progressService.createPercentageProgressTask(
					"validate-" + javaProject.getElementName(),
					applicableValidators.size(),
					"Validating Spring Boot Version of project '%s'".formatted(javaProject.getElementName()));
			
			try {
				Version javaProjectVersion = SpringProjectUtil.getSpringBootVersion(javaProject);

				if (javaProjectVersion == null) {
					log.warn("Unable to resolve version for project: " + javaProject.getLocationUri().toASCIIString());
					return new DiagnosticResult(buildFileUri, Collections.emptyList());
				}

				for (VersionValidator validator : applicableValidators) {
					try {
						Collection<Diagnostic> batch = validator.validate(javaProject, javaProjectVersion);
						if (batch != null) {
							diagnostics.addAll(batch);
						}
					} catch (Exception e) {
						messageService.error("Failed Spring Boot version validation for project '%s': %s".formatted(javaProject.getElementName(), e.getMessage()));
						log.error("", e);
					}
					progress.increment();
				}
			} finally {
				progress.done();
			}
		}
		
		return new DiagnosticResult(buildFileUri, diagnostics);
	}

	protected File getSpringBootDependency(IJavaProject project) {
		return project.getClasspath().findBinaryLibrary(SpringProjectUtil.SPRING_BOOT).map(cpe -> new File(cpe.getPath())).orElse(null);
	}

	public static class DiagnosticResult {
		
		private final URI documentUri;
		private final List<Diagnostic> diagnostics;
		
		
		public DiagnosticResult(URI documentUri, List<Diagnostic> diagnostics) {
			super();
			this.documentUri = documentUri;
			this.diagnostics = diagnostics;
		}


		public URI getDocumentUri() {
			return documentUri;
		}


		public List<Diagnostic> getDiagnostics() {
			return diagnostics;
		}
		
	}
}
