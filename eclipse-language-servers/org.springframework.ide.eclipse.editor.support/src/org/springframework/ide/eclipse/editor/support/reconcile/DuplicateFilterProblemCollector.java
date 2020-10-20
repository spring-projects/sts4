/*******************************************************************************
 * Copyright (c) 2017 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.editor.support.reconcile;

import java.util.HashSet;
import java.util.Set;

public class DuplicateFilterProblemCollector implements IProblemCollector {

	private Set<ReconcileProblem> seen = new HashSet<>();

	private final IProblemCollector delegate;

	public DuplicateFilterProblemCollector(IProblemCollector delegate) {
		this.delegate = delegate;
	}

	@Override
	public void beginCollecting() {
		seen.clear();
		delegate.beginCollecting();
	}

	@Override
	public void endCollecting() {
		delegate.endCollecting();
		seen.clear();
	}

	@Override
	public void accept(ReconcileProblem problem) {
		if (seen.add(problem)) {
			delegate.accept(problem);
		}
	}

}
