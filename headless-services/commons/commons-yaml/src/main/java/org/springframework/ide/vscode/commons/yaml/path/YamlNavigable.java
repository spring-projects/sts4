/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
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

import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;

/**
 * Different types of things (e.g. {@link ApplicationYamlAssistContext}, {@link SNode} ...) can
 * be traversed interpeting {@link YamlPath} as 'navigation operations'. To facilitate
 * 'reusable' traversal code, they can implement this interface.
 * <p>
 * WARNING: both methods in this interface have default implementation. However at least one
 * of them must be implemented explicitly otherwise they will call eachother in an infinite recursion!
 */
public interface YamlNavigable<T> {
	
	/**
	 * Traversal which silently ignores ambiguity by picking the first valid target
	 * returned by traverseAmbiguously.
	 * 
	 */
	default T traverse(YamlPathSegment s) throws Exception {
		return traverseAmbiguously(s).findFirst().orElse(null);
	}
	
	/**
	 * To support traversal in the face of ambiguous steps (i.e. when a step may lead to multiple valid targets), 
	 * implement this method. For convenience a default implementation is provided which calls `traverse`. 
	 * Obviously, this implementation doesn't truly support ambiguity but it is sufficient for YamlNavigables
	 * where there is no ambiguity, or if you don't care about it.
	 */
	default Stream<T> traverseAmbiguously(YamlPathSegment s) {
		try {
			T it = traverse(s);
			return it == null ? Stream.empty() : Stream.of(it);
		} catch (Exception e) {
			Log.log(e);
			return Stream.empty();
		}
	}
}
