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
import org.springframework.ide.vscode.commons.java.Version;

public class VersionValidation {

	private final Version versionToUpgrade;
	private final boolean enabled;
	private final DiagnosticSeverity severity;
	private final String message;

	public VersionValidation(Version versionToUpgrade, boolean enabled, DiagnosticSeverity severity, String message) {
		this.versionToUpgrade = versionToUpgrade;
		this.enabled = enabled;
		this.severity = severity;
		this.message = message;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public DiagnosticSeverity getSeverity() {
		return this.severity;
	}

	public Version getVersionToUprade() {
		return this.versionToUpgrade;
	}

	public String getMessage() {
		return this.message;
	}

}
