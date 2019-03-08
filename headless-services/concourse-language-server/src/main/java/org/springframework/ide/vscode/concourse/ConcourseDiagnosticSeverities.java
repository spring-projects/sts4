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
package org.springframework.ide.vscode.concourse;

import org.eclipse.lsp4j.DiagnosticSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.DiagnosticSeverityProvider;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.yaml.reconcile.YamlSchemaProblems;
import org.springframework.stereotype.Component;

@Component
public class ConcourseDiagnosticSeverities implements DiagnosticSeverityProvider {

	@Override
	public DiagnosticSeverity getDiagnosticSeverity(ReconcileProblem problem) {
		ProblemType type = problem.getType();
		if (YamlSchemaProblems.PROPERTY_CONSTRAINT.contains(type)) {
			return DiagnosticSeverity.Warning;
		}
		return DEFAULT.getDiagnosticSeverity(problem);
	}

}
