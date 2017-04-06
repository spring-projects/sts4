/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.quickfix;

import java.util.List;

import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

/**
 * Represents a strategy for computing potential quickfixes for a given problem.
 *
 * @author Kris De Volder
 */
@FunctionalInterface
public interface ProblemFixer {

	/**
	 * Implementor can inspect the problem and quickfix context provided as parameters.
	 * <p>
	 * If the problem is deemed fixable, the strategy can contribute one or more fixes by
	 * adding them to the list of proposals (provided as third parameter).
	 */
	void contributeFixes(
			QuickfixContext context, ReconcileProblem problem,
			List<ICompletionProposal> proposals
	);

}
