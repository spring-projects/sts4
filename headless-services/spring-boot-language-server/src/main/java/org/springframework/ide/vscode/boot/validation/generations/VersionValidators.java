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

import java.util.Arrays;
import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.Version;

public class VersionValidators {
	
	private final VersionValidator[] validators;

	public VersionValidators(VersionValidationPreferences preferences) {
		this.validators = new VersionValidator[] {
				new SupportedValidator(preferences), new UnsupportedCommercialValidator(preferences),
				new UnsupportedOssValidator(preferences), new UnsupportedValidator(preferences)
		};
	}

	public List<VersionValidator> getValidators() {
		return Arrays.asList(this.validators);
	}

	private static class SupportedValidator implements VersionValidator {

		private final VersionValidationPreferences preferences;

		public SupportedValidator(VersionValidationPreferences preferences) {
			this.preferences = preferences;
		}

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {

			if (VersionValidationUtils.isCommercialValid(generation) && VersionValidationUtils.isOssValid(generation)) {
				DiagnosticSeverity severity = preferences.getSupportedPreference().getSeverity();

				StringBuffer message = new StringBuffer();

				message.append("OSS support ends on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Commercial supports ends on: ");
				message.append(generation.getCommercialSupportEndDate());
				
				Version toUpdate = VersionValidationUtils.getLatestSupportedRelease(springProject, version);
				return new VersionValidation(toUpdate, severity, message.toString());
			}
			return null;
		}

		@Override
		public boolean isEnabled() {
			return preferences.getSupportedPreference().isEnabled();
		}
	}

	private static class UnsupportedValidator implements VersionValidator {

		private final VersionValidationPreferences preferences;

		public UnsupportedValidator(VersionValidationPreferences preferences) {
			this.preferences = preferences;
		}

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {

			if (!VersionValidationUtils.isCommercialValid(generation)
					&& !VersionValidationUtils.isOssValid(generation)) {
				DiagnosticSeverity severity = preferences.getUnsupportedPreference().getSeverity();
				StringBuffer message = new StringBuffer();

				message.append("Unsupported OSS. Support ended on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(generation.getCommercialSupportEndDate());

				return new VersionValidation(
						toUpdateForUnsupported(springProject, version), severity, message.toString());
			}
			return null;
		}

		@Override
		public boolean isEnabled() {
			return preferences.getUnsupportedPreference().isEnabled();
		}
	}

	private static class UnsupportedOssValidator implements VersionValidator {

		private final VersionValidationPreferences preferences;

		public UnsupportedOssValidator(VersionValidationPreferences preferences) {
			this.preferences = preferences;
		}

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {
			if (!VersionValidationUtils.isOssValid(generation)
					&& VersionValidationUtils.isCommercialValid(generation)) {
				
				DiagnosticSeverity severity = preferences.getUnsupportedOssPreference().getSeverity();

				StringBuffer message = new StringBuffer();
				message.append("Unsupported OSS. Support ended on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Commercial supports ends on: ");
				message.append(generation.getCommercialSupportEndDate());
				return new VersionValidation(toUpdateForUnsupported(springProject, version), severity, message.toString());

			}
			return null;
		}

		@Override
		public boolean isEnabled() {
			return preferences.getUnsupportedOssPreference().isEnabled();
		}
	}

	private static class UnsupportedCommercialValidator implements VersionValidator {
		
		private final VersionValidationPreferences preferences;

		public UnsupportedCommercialValidator(VersionValidationPreferences preferences) {
			this.preferences = preferences;
		}

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {
			if (!VersionValidationUtils.isCommercialValid(generation)
					&& VersionValidationUtils.isOssValid(generation)) {

				DiagnosticSeverity severity = preferences.getUnsupportedCommercialPreference().getSeverity();

				StringBuffer message = new StringBuffer();
				message.append("OSS support ends on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(generation.getCommercialSupportEndDate());
				
				return new VersionValidation(toUpdateForUnsupported(springProject, version), severity, message.toString());
			}
			return null;
		}

		@Override
		public boolean isEnabled() {
			return preferences.getUnsupportedCommercialPreference().isEnabled();
		}
	}

	private static Version toUpdateForUnsupported(ResolvedSpringProject springProject, Version version)
			throws Exception {
		Version toUpdate = VersionValidationUtils.getLatestSupportedInSameMajor(springProject, version);
		if (toUpdate == null) {
			toUpdate = VersionValidationUtils.getLatestSupportedRelease(springProject, version);
		}
		return toUpdate;
	}
	
}
