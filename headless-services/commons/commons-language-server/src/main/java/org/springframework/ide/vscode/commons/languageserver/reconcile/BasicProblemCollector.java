/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.reconcile;

import java.util.Collection;

public class BasicProblemCollector implements IProblemCollector {
	
	private final Collection<ReconcileProblem> problems;

	public BasicProblemCollector(Collection<ReconcileProblem> problems) {
		this.problems = problems;
	}

	@Override
	public void beginCollecting() {
	}

	@Override
	public void endCollecting() {
	}

	@Override
	public void accept(ReconcileProblem problem) {
		problems.add(problem);
	}

}
