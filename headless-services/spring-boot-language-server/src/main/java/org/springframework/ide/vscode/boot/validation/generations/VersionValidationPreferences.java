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

import org.eclipse.lsp4j.DiagnosticSeverity;

// TODO: integrate with actual LS preferences
public class VersionValidationPreferences {

	private static final String DEFAULT_SPRING_PROJECT_URL = "https://spring.io/api/projects";

	public ValidationPreference getSupportedPreference() {
		return new ValidationPreference(true, DiagnosticSeverity.Hint);
	}

	public ValidationPreference getUnsupportedOssPreference() {
		return new ValidationPreference(true, DiagnosticSeverity.Warning);
	}

	public ValidationPreference getUnsupportedCommercialPreference() {
		return new ValidationPreference(true, DiagnosticSeverity.Warning);
	}

	public ValidationPreference getUnsupportedPreference() {
		return new ValidationPreference(true, DiagnosticSeverity.Error);
	}

	public String getSpringProjectsUrl() {
		return DEFAULT_SPRING_PROJECT_URL;
	}
}
