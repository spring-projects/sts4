/*******************************************************************************
 * Copyright (c) 2014-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

public interface IProblemCollector {

	void beginCollecting();
	void endCollecting();
	void accept(ReconcileProblem problem);

	/**
	 * Allows the problem collector to process problems that has been collected so
	 * far, BEFORE the end collecting. It is to handle cases where a subset of
	 * problems need to be processed or published in an "intermediate" phase during
	 * a collecting session, but prior to the final end collecting. For example, if
	 * a collection session in a reconcile engine wants to publish fast problems
	 * first before handling slow problems , this method allows the reconcile engine
	 * to notify the problem collector to process the fast problems first before
	 * starting with the slow ones. The reconcile engine, or whoever is calling the
	 * collector, is responsible for deciding when to call this checkpoint.
	 */
	default void checkPointCollecting() {

	}

	/**
	 * Problem collector that simply ignores/discards anything passed to it.
	 */
	IProblemCollector NULL = new IProblemCollector() {
		public void beginCollecting() {
		}
		public void endCollecting() {
		}
		public void accept(ReconcileProblem problem) {
		}
	};
}