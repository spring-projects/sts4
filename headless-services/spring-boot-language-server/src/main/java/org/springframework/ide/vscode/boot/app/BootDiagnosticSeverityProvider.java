/*******************************************************************************
 * Copyright (c) 2020, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.stereotype.Component;

@Component
public class BootDiagnosticSeverityProvider implements DiagnosticSeverityProvider {

	private BootJavaConfig config;
	
	public BootDiagnosticSeverityProvider(BootJavaConfig config) {
		this.config = config;
	}
	
	@Override
	public synchronized DiagnosticSeverity getDiagnosticSeverity(ProblemType problem) {
		String severityOverride = config.getRawSettings().getString("spring-boot", "ls", "problem", problem.getCategory().getId(), problem.getCode());

		ProblemSeverity severity = null;
		if (severityOverride != null && !severityOverride.isBlank()) {
			severity = ProblemSeverity.valueOf(severityOverride);
		}

		if (severity == null) {
			severity = problem.getDefaultSeverity();
		}

		return DiagnosticSeverityProvider.diagnosticSeverity(severity);
	}
}
