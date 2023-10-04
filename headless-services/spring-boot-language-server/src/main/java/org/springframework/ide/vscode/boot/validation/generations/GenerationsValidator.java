/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.util.Collection;
import java.util.List;

import org.eclipse.lsp4j.Diagnostic;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationProblemType;
import org.springframework.ide.vscode.commons.Version;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.util.Assert;

import com.google.common.collect.ImmutableList;

public class GenerationsValidator extends AbstractDiagnosticValidator {
	
	private SpringProjectsProvider provider;

	public GenerationsValidator(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
		super(diagnosticSeverityProvider);
		this.provider = provider;
	}
	
	private static Generation getGenerationForJavaProject(IJavaProject javaProject, ResolvedSpringProject springProject)
			throws Exception {
		List<Generation> genList = springProject.getGenerations();
		Version javaProjectVersion = SpringProjectUtil.getDependencyVersion(javaProject, springProject.getSlug());

		// Find the generation belonging to the dependency
		for (Generation gen : genList) {
			Version genVersion = SpringProjectUtil.getVersionFromGeneration(gen.getName());
			if (genVersion.getMajor() == javaProjectVersion.getMajor()
					&& genVersion.getMinor() == javaProjectVersion.getMinor()) {
				return gen;
			}
		}
		return null;
	}

	@Override
	public Collection<Diagnostic> validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
		ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
		Generation javaProjectGen = getGenerationForJavaProject(javaProject, springProject);
		Assert.isLegal(javaProjectGen != null, "Unable to find Spring Project Generation for project: " + javaProjectVersion.toString());
		ImmutableList.Builder<Diagnostic> b = ImmutableList.builder();
		
		if (VersionValidationUtils.isOssValid(javaProjectGen)) {
			StringBuilder message = new StringBuilder();
			message.append("OSS support for Spring Boot ");
			message.append(javaProjectGen.getName());
			message.append(" ends on: ");
			message.append(javaProjectGen.getOssSupportEndDate());
			Diagnostic d = createDiagnostic(VersionValidationProblemType.SUPPORTED_OSS_VERSION, message.toString());
			if (d != null) {
				b.add(d);
			}
		} else {
			StringBuilder message = new StringBuilder();
			message.append("OSS support for Spring Boot ");
			message.append(javaProjectGen.getName());
			message.append(" no longer available, ended on: ");
			message.append(javaProjectGen.getOssSupportEndDate());
			Diagnostic d = createDiagnostic(VersionValidationProblemType.UNSUPPORTED_OSS_VERSION, message.toString());
			if (d != null) {
				b.add(d);
			}
		}

		if (VersionValidationUtils.isCommercialValid(javaProjectGen)) {
			StringBuilder message = new StringBuilder();
			message.append("Commercial support for Spring Boot ");
			message.append(javaProjectGen.getName());
			message.append(" ends on: ");
			message.append(javaProjectGen.getCommercialSupportEndDate());
			Diagnostic d = createDiagnostic(VersionValidationProblemType.SUPPORTED_COMMERCIAL_VERSION, message.toString());
			if (d != null) {
				b.add(d);
			}
		} else {
			StringBuilder message = new StringBuilder();
			message.append("Commercial support for Spring Boot ");
			message.append(javaProjectGen.getName());
			message.append(" no longer available, ended on: ");
			message.append(javaProjectGen.getCommercialSupportEndDate());
			Diagnostic d = createDiagnostic(VersionValidationProblemType.UNSUPPORTED_COMMERCIAL_VERSION, message.toString());
			if (d != null) {
				b.add(d);
			}
		}

		return b.build();
	}

}
