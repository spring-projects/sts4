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
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;

public class ProjectVersionDiagnosticProvider {

	private final SpringProjectsProvider provider;
	private final VersionValidators validators;

	public ProjectVersionDiagnosticProvider(SpringProjectsProvider provider, VersionValidators validators) {
		this.validators = validators;
		this.provider = provider;
	}


	public DiagnosticResult getDiagnostics(IJavaProject javaProject) throws Exception {

		URI buildFileUri = javaProject.getProjectBuild() == null ? null : javaProject.getProjectBuild().getBuildFile();
		if (buildFileUri == null) {
			throw new Exception("Unable to find build file in project while computing version validation for: ");
		}

		ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
		Version javaProjectVersion = SpringProjectUtil.getSpringBootVersion(javaProject);

		if (javaProjectVersion == null) {
			throw new Exception("Unable to resolve version for project: " + javaProject.getLocationUri().toString());
		}

		List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

		for (VersionValidator validator : validators.getValidators()) {
			Diagnostic diagnostic = validator.validate(springProject, javaProject, javaProjectVersion);
			if (diagnostic != null) {
				diagnostics.add(diagnostic);
			}
		}

		return new DiagnosticResult(buildFileUri, diagnostics);
	}

	protected File getSpringBootDependency(IJavaProject project) {
		List<File> libs = SpringProjectUtil.getLibrariesOnClasspath(project, "spring-boot");
		return libs != null && libs.size() > 0 ? libs.get(0) : null;
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
