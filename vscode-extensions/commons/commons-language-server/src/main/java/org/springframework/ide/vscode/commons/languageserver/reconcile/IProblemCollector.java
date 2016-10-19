/*******************************************************************************
 * Copyright (c) 2014-2016 Pivotal, Inc.
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