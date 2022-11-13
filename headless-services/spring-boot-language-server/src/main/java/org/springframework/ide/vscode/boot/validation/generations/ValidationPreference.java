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

public class ValidationPreference {
	
	private final boolean enabled;
	private final DiagnosticSeverity severity;
	
	public ValidationPreference(boolean enabled, DiagnosticSeverity severity) {
		super();
		this.enabled = enabled;
		this.severity = severity;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public DiagnosticSeverity getSeverity() {
		return severity;
	}
}
