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

import java.util.List;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.ResolvedSpringProject;
import org.springframework.ide.vscode.commons.java.Version;

import com.google.common.collect.ImmutableList;

public class VersionValidators {

	public List<VersionValidator> getValidators() {
		return ImmutableList.of(new SupportedValidator(), new UnsupportedCommercialValidator(),
				new UnsupportedOssValidator(), new UnsupportedValidator());
	}

	private static class SupportedValidator implements VersionValidator {

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {

			if (VersionValidationUtils.isCommercialValid(generation) && VersionValidationUtils.isOssValid(generation)) {
				DiagnosticSeverity severity = DiagnosticSeverity.Information;
				boolean enabled = true;

				StringBuffer message = new StringBuffer();

				message.append("OSS support ends on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Commercial supports ends on: ");
				message.append(generation.getCommercialSupportEndDate());
				Version toUpdate = VersionValidationUtils.getLatestSupportedRelease(springProject, version);
				return new VersionValidation(toUpdate, enabled, severity, message.toString());
			}
			return null;
		}
	}

	private static class UnsupportedValidator implements VersionValidator {

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {

			if (!VersionValidationUtils.isCommercialValid(generation)
					&& !VersionValidationUtils.isOssValid(generation)) {
				DiagnosticSeverity severity = DiagnosticSeverity.Error;
				boolean enabled = true;
				StringBuffer message = new StringBuffer();

				message.append("Unsupported OSS. Support ended on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(generation.getCommercialSupportEndDate());

				return new VersionValidation(
						toUpdateForUnsupported(springProject, version), enabled, severity, message.toString());
			}
			return null;
		}
	}

	private static class UnsupportedOssValidator implements VersionValidator {

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {
			if (!VersionValidationUtils.isOssValid(generation)
					&& VersionValidationUtils.isCommercialValid(generation)) {
				DiagnosticSeverity severity = DiagnosticSeverity.Warning;
				boolean enabled = true;

				StringBuffer message = new StringBuffer();
				message.append("Unsupported OSS. Support ended on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Commercial supports ends on: ");
				message.append(generation.getCommercialSupportEndDate());
				return new VersionValidation(toUpdateForUnsupported(springProject, version), enabled, severity, message.toString());

			}
			return null;
		}
	}

	private static class UnsupportedCommercialValidator implements VersionValidator {

		@Override
		public VersionValidation getValidation(ResolvedSpringProject springProject, Generation generation,
				Version version) throws Exception {
			if (!VersionValidationUtils.isCommercialValid(generation)
					&& VersionValidationUtils.isOssValid(generation)) {

				DiagnosticSeverity severity = DiagnosticSeverity.Warning;
				boolean enabled = true;

				StringBuffer message = new StringBuffer();
				message.append("OSS support ends on: ");
				message.append(generation.getOssSupportEndDate());
				message.append('\n');
				message.append("Unsupported Commercial. Support ended on: ");
				message.append(generation.getCommercialSupportEndDate());
				return new VersionValidation(toUpdateForUnsupported(springProject, version), enabled, severity, message.toString());
			}
			return null;
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
