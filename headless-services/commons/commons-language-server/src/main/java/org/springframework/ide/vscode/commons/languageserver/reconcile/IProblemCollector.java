/*******************************************************************************
 * Copyright (c) 2014-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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
	 * Optional for both implementors and callers.
	 * <p/>
	 * This method optionally allows callers to do partial collection between the
	 * start and end collecting, and can be called numerous times. The caller is
	 * responsible to decide when and how often these checkpoints are invoked during
	 * a collecting session.
	 * <p/>
	 * For implementors, this optional support handles cases where problems need to be processed in
	 * intermediate phases between the start and end collecting stages, and if
	 * implemented, should support multiple checkpoint invocations.
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