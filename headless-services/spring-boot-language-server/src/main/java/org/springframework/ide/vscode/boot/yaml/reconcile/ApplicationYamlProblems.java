/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.boot.yaml.reconcile;

import static org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity.ERROR;

import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;

public class ApplicationYamlProblems {
	
	public static enum Type implements ProblemType {
		
		YAML_SYNTAX_ERROR;

		Type() {
			this(ERROR);
		}

		Type(ProblemSeverity defaultSeverity) {
			this.severity = defaultSeverity;
		}
		
		private final ProblemSeverity severity;

		@Override
		public ProblemSeverity getDefaultSeverity() {
			return severity;
		}

		@Override
		public String getCode() {
			return name();
		}
		
	}
	
	public static final String YAML_SYNTAX_ERROR = "YAML_SYNTAX_ERROR";

	public static ReconcileProblem problem(Type type, String msg, int offset, int len) {
		return new ReconcileProblemImpl(type, msg, offset, len);
	}
}
