/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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

import org.springframework.ide.vscode.commons.util.Assert;

public class AlternativeYamlTraversal extends AbstractYamlTraversal {

	private YamlTraversal first;
	private YamlTraversal second;

	public AlternativeYamlTraversal(YamlTraversal first, YamlTraversal second) {
		Assert.isLegal(!first.isEmpty());
		Assert.isLegal(!second.isEmpty());
		this.first = first;
		this.second = second;
	}

	@Override
	public <T extends YamlNavigable<T>> Stream<T> traverseAmbiguously(T start) {
		return Stream.concat(
				first.traverseAmbiguously(start),
				second.traverseAmbiguously(start)
		);
	}

	@Override
	public boolean canEmpty() {
		return first.canEmpty() || second.canEmpty();
	}

	@Override
	public String toString() {
		return "Or("+first+", "+second+")";
	}

}
