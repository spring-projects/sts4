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

import java.util.Arrays;
import java.util.List;

import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;

/* 
 * TODO: Once uncommented to bring in Spring.io generations API support:
 * 1. Remaining validators must become beans.
 * 2. I'd merge all 4 into one single validator
 */
public class VersionValidators {

	private final VersionValidator[] validators;

	public VersionValidators(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
		this.validators = new VersionValidator[] {
//				new SupportedOssValidator(diagnosticSeverityProvider, provider),
//				new UnsupportedCommercialValidator(diagnosticSeverityProvider, provider),
//				new UnsupportedOssValidator(diagnosticSeverityProvider, provider),
//				new SupportedCommercialValidator(diagnosticSeverityProvider, provider),
		};
	}

	public List<VersionValidator> getValidators() {
		return Arrays.asList(this.validators);
	}
	
//	private static Generation getGenerationForJavaProject(IJavaProject javaProject, ResolvedSpringProject springProject)
//			throws Exception {
//		List<Generation> genList = springProject.getGenerations();
//		Version javaProjectVersion = SpringProjectUtil.getDependencyVersion(javaProject, springProject.getSlug());
//
//		// Find the generation belonging to the dependency
//		for (Generation gen : genList) {
//			Version genVersion = SpringProjectUtil.getVersionFromGeneration(gen.getName());
//			if (genVersion.getMajor() == javaProjectVersion.getMajor()
//					&& genVersion.getMinor() == javaProjectVersion.getMinor()) {
//				return gen;
//			}
//		}
//		return null;
//	}
//
//	private static class SupportedOssValidator extends AbstractDiagnosticValidator {
//
//		private SpringProjectsProvider provider;
//
//		public SupportedOssValidator(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
//			super(diagnosticSeverityProvider);
//			this.provider = provider;
//		}
//
//		@Override
//		public Diagnostic validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
//			ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
//			Generation javaProjectGen = getGenerationForJavaProject(javaProject, springProject);
//			Assert.isLegal(javaProjectGen != null, "Unable to find Spring Project Generation for project: " + javaProjectVersion.toString());
//			if (VersionValidationUtils.isOssValid(javaProjectGen)) {
//				VersionValidationProblemType problemType = VersionValidationProblemType.SUPPORTED_OSS_VERSION;
//
//				StringBuffer message = new StringBuffer();
//				message.append("OSS support for Spring Boot " + javaProjectGen.getName() + " ends on: ");
//				message.append(javaProjectGen.getOssSupportEndDate());
//
//				return createDiagnostic(problemType, message.toString());
//			}
//			return null;
//		}
//	}
//
//	private static class SupportedCommercialValidator extends AbstractDiagnosticValidator {
//
//		private SpringProjectsProvider provider;
//
//		public SupportedCommercialValidator(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
//			super(diagnosticSeverityProvider);
//			this.provider = provider;
//		}
//
//		@Override
//		public Diagnostic validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
//			ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
//			Generation javaProjectGen = getGenerationForJavaProject(javaProject, springProject);
//			Assert.isLegal(javaProjectGen != null, "Unable to find Spring Project Generation for project: " + javaProjectVersion.toString());
//
//			if (VersionValidationUtils.isCommercialValid(javaProjectGen)) {
//
//				VersionValidationProblemType problemType = VersionValidationProblemType.SUPPORTED_COMMERCIAL_VERSION;
//
//				StringBuffer message = new StringBuffer();
//				message.append("Commercial support for Spring Boot " + javaProjectGen.getName() + " ends on: ");
//				message.append(javaProjectGen.getCommercialSupportEndDate());
//
//				return createDiagnostic(problemType, message.toString());
//			}
//			return null;
//		}
//	}
//
//	private static class UnsupportedOssValidator extends AbstractDiagnosticValidator {
//
//		private SpringProjectsProvider provider;
//
//		public UnsupportedOssValidator(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
//			super(diagnosticSeverityProvider);
//			this.provider = provider;
//		}
//
//		@Override
//		public Diagnostic validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
//			ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
//			Generation javaProjectGen = getGenerationForJavaProject(javaProject, springProject);
//			Assert.isLegal(javaProjectGen != null, "Unable to find Spring Project Generation for project: " + javaProjectVersion.toString());
//
//			if (!VersionValidationUtils.isOssValid(javaProjectGen)) {
//
//				VersionValidationProblemType problemType = VersionValidationProblemType.UNSUPPORTED_OSS_VERSION;
//
//				StringBuffer message = new StringBuffer();
//				message.append("OSS support for Spring Boot " + javaProjectGen.getName() + " no longer available, ended on: ");
//				message.append(javaProjectGen.getOssSupportEndDate());
//
//				return createDiagnostic(problemType, message.toString());
//			}
//			return null;
//		}
//	}
//
//	private static class UnsupportedCommercialValidator extends AbstractDiagnosticValidator {
//
//		private SpringProjectsProvider provider;
//
//		public UnsupportedCommercialValidator(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
//			super(diagnosticSeverityProvider);
//			this.provider = provider;
//		}
//
//		@Override
//		public Diagnostic validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
//			ResolvedSpringProject springProject = provider.getProject(SpringProjectUtil.SPRING_BOOT);
//			Generation javaProjectGen = getGenerationForJavaProject(javaProject, springProject);
//			Assert.isLegal(javaProjectGen != null, "Unable to find Spring Project Generation for project: " + javaProjectVersion.toString());
//
//			if (!VersionValidationUtils.isCommercialValid(javaProjectGen)) {
//
//				VersionValidationProblemType problemType = VersionValidationProblemType.UNSUPPORTED_COMMERCIAL_VERSION;
//
//				StringBuffer message = new StringBuffer();
//				message.append("Commercial support for Spring Boot " + javaProjectGen.getName() + " no longer available, ended on: ");
//				message.append(javaProjectGen.getCommercialSupportEndDate());
//
//				return createDiagnostic(problemType, message.toString());
//			}
//			return null;
//		}
//	}

}

