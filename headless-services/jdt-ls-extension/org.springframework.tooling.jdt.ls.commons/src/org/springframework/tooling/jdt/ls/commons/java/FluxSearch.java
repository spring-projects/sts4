/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.jdt.ls.commons.java;

import java.time.Duration;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import reactor.core.publisher.Flux;

public interface FluxSearch<T> {

	Flux<T> search(IJavaProject project, String searchTerm, String searchType);

	default List<T> searchWithLimits(IJavaProject javaProject, String searchTerm, String searchType, long timeLimit) {
		Flux<T> flux = this.search(javaProject, searchTerm, searchType);
		if (timeLimit > 0) {
			flux = flux.take(Duration.ofMillis(timeLimit));
		}
		return flux.collectList().block();
	}

}
