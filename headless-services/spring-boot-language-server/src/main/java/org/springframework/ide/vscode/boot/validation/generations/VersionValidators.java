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
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.springframework.ide.vscode.boot.java.rewrite.SpringBootUpgrade;
import org.springframework.ide.vscode.boot.validation.generations.preferences.VersionValidationProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;

import com.google.common.collect.ImmutableList;

public class VersionValidators {

	public static final String BOOT_VERSION_VALIDATION_CODE = "BOOT_VERSION_VALIDATION_CODE";

	private final VersionValidator[] validators;

	public VersionValidators(DiagnosticSeverityProvider diagnosticSeverityProvider, SpringProjectsProvider provider) {
		this.validators = new VersionValidator[] {
//				new SupportedOssValidator(diagnosticSeverityProvider, provider),
//				new UnsupportedCommercialValidator(diagnosticSeverityProvider, provider),
//				new UnsupportedOssValidator(diagnosticSeverityProvider, provider),
//				new SupportedCommercialValidator(diagnosticSeverityProvider, provider),
				new UpdateBootVersion(diagnosticSeverityProvider)
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

	
	private static class UpdateBootVersion extends AbstractDiagnosticValidator {

		public UpdateBootVersion(DiagnosticSeverityProvider diagnosticSeverityProvider) {
			super(diagnosticSeverityProvider);
		}

		@Override
		public Collection<Diagnostic> validate(IJavaProject javaProject, Version javaProjectVersion) throws Exception {
			List<Version> versions = CachedBootVersionsFromMavenCentral.getBootVersions();
			ImmutableList.Builder<Diagnostic> builder = ImmutableList.builder();
			validateMajorVersion(javaProject, javaProjectVersion, versions).ifPresent(builder::add);
			validateMinorVersion(javaProject, javaProjectVersion, versions).ifPresent(builder::add);
			validatePatchVersion(javaProject, javaProjectVersion, versions).ifPresent(builder::add);
			return builder.build();
		}
		
		private Optional<Diagnostic> validateMajorVersion(IJavaProject javaProject, Version javaProjectVersion, List<Version> sortedBootVersions) {
			Version latest = VersionValidationUtils.getNewerLatestMajorRelease(sortedBootVersions, javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_MAJOR_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer major version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Spring Boot " + latest.toString() + " (executes the full project conversion recipe from OpenRewrite)");
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toASCIIString(), latest.toString())));

				
				return Optional.of(createDiagnostic(ca, problemType, message.toString()));
			}
			return Optional.empty();
		}

		private Optional<Diagnostic> validateMinorVersion(IJavaProject javaProject, Version javaProjectVersion, List<Version> sortedBootVersions) {
			Version latest = VersionValidationUtils.getNewerLatestMinorRelease(sortedBootVersions, javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_MINOR_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer minor version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Spring Boot " + latest.toString() + " (executes the full project conversion recipe from OpenRewrite)");
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toASCIIString(), latest.toString())));

				
				return Optional.of(createDiagnostic(ca, problemType, message.toString()));
			}
			return Optional.empty();
		}

		private Optional<Diagnostic> validatePatchVersion(IJavaProject javaProject, Version javaProjectVersion, List<Version> sortedBootVersions) {
			Version latest = VersionValidationUtils.getNewerLatestPatchRelease(sortedBootVersions, javaProjectVersion);

			if (latest != null) {
				VersionValidationProblemType problemType = VersionValidationProblemType.UPDATE_LATEST_PATCH_VERSION;

				StringBuffer message = new StringBuffer();
				message.append("Newer patch version of Spring Boot available: ");
				message.append(latest.toString());

				CodeAction ca = new CodeAction();
				ca.setKind(CodeActionKind.QuickFix);
				ca.setTitle("Upgrade to Spring Boot " + latest.toString() + " (Maven dependency version changes only)");
				String commandId = SpringBootUpgrade.CMD_UPGRADE_SPRING_BOOT;
				ca.setCommand(new Command("Upgrade to Version " + latest.toString(), commandId,
						ImmutableList.of(javaProject.getLocationUri().toASCIIString(), latest.toString())));

				
				return Optional.of(createDiagnostic(ca, problemType, message.toString()));
			}
			return Optional.empty();
		}
	}
}

