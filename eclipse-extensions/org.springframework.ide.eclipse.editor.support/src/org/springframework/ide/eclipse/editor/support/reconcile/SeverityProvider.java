/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

/**
 * Provides a means to map {@link ReconcileProblem} to a {@link ProblemSeverity}. Problems mapped to 'IGNORE'
 * severity will suppressed.
 *
 * @author Kris De Volder
 */
public interface SeverityProvider {

	ProblemSeverity getSeverity(ProblemType problemType);

	/**
	 * Called at the start of a reconciling/validation session. This can be used by SeverityProviders that cache severities
	 * as a good time clear the cache.
	 */
	void startSession();

	default ProblemSeverity getSeverity(ReconcileProblem problem) {
		return getSeverity(problem.getType());
	}
}
