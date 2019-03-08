/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.path;

import java.util.stream.Stream;

public class SequencingYamlTraversal extends AbstractYamlTraversal {

	private YamlTraversal first;
	private YamlTraversal second;

	public SequencingYamlTraversal(YamlTraversal first, YamlTraversal second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public <T extends YamlNavigable<T>> Stream<T> traverseAmbiguously(T start) {
		return first.traverseAmbiguously(start)
				.flatMap(second::traverseAmbiguously);
	}

	@Override
	public boolean canEmpty() {
		return first.canEmpty() && second.canEmpty();
	}

}
