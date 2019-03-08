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

/**
 * @author Kris De Volder
 */
public class FilteringTraversal extends AbstractYamlTraversal {

	private YamlTraversal yamlTraversal;
	private YamlTraversal check;

	public FilteringTraversal(YamlTraversal yamlTraversal, YamlTraversal check) {
		this.yamlTraversal = yamlTraversal;
		this.check = check;
	}

	@Override
	public <T extends YamlNavigable<T>> Stream<T> traverseAmbiguously(T start) {
		Stream<T> x = yamlTraversal.traverseAmbiguously(start);
		return x.filter(target ->
			check.traverseAmbiguously(target)
			.findAny()
			.isPresent()
		);
	}

	@Override
	public String toString() {
		return "Filter("+yamlTraversal + "has: " + check + ")";
	}

	@Override
	public boolean canEmpty() {
		return yamlTraversal.canEmpty();
	}

}
