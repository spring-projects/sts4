/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

/**
 * Exception if there is a failure when parsing a value. It does not wrap
 * other exceptions such that when thrown, the parse exception is the "deepest"
 * error.
 *
 */
public class ReconcileException extends Exception implements ProblemTypeProvider {

	private static final long serialVersionUID = 1L;
	private final ProblemType problemType;

	public ReconcileException(String message, ProblemType problemType) {
		super(message);
		this.problemType = problemType;
	}

	@Override
	public ProblemType getProblemType() {
		return problemType;
	}
}
