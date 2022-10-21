/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

import org.eclipse.lsp4j.DiagnosticSeverity;

@FunctionalInterface
public interface DiagnosticSeverityProvider {
	
	static DiagnosticSeverity diagnosticSeverity(ProblemSeverity severity) {
		switch (severity) {
		case ERROR:
			return DiagnosticSeverity.Error;
		case WARNING:
			return DiagnosticSeverity.Warning;
		case INFO:
			return DiagnosticSeverity.Information;
		case HINT:
			return DiagnosticSeverity.Hint;
		case IGNORE:
			return null;
		default:
			throw new IllegalStateException("Bug! Missing switch case?");
		}
	}
	
	DiagnosticSeverity getDiagnosticSeverity(ProblemType problem);

	default DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
		return getDiagnosticSeverity(problem.getType());
	}
	
	static final DiagnosticSeverityProvider DEFAULT = (problem) -> {
		ProblemSeverity severity = problem.getDefaultSeverity();
		return diagnosticSeverity(severity);
	};
	
}
