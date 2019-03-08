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

import org.springframework.ide.vscode.commons.util.Streams;

/**
 * Abstract superclass for implementing concrete {@link YamlTraversal}s.
 * <p>
 * Note that allthough this class provides a default implementation for both
 * `traverse` and `traverseAmbiguously`, at least one of these methods <b>must<b>
 * be overridden by the subclass (otherwise the methods will just call eachother
 * in a infinite recursion loop).
 * <p>
 * To implement a non-ambiguous traversal, override the `traverse` method.
 * <p>
 * To implement an ambguous traversal, override the `traverseAmbiguously` method
 * instead.
 *
 * @author Kris De Volder
 */
public abstract class AbstractYamlTraversal implements YamlTraversal {

	@Override
	public <T extends YamlNavigable<T>> Stream<T> traverseAmbiguously(T start) {
		return Streams.fromNullable(traverse(start));
	}

	@Override
	public <T extends YamlNavigable<T>> T traverse(T startNode) {
		return traverseAmbiguously(startNode).findFirst().orElse(null);
	}

}
