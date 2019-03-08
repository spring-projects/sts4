/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.path;

import java.util.Collections;

/**
 * Provides a way to compute all the 'aliases' that are considered 'equivalent' to a
 * given key. This is used by structure traversals to try alternatives if the exact
 * name itself doesn't correspond to a node in the tree.
 * <p>
 * A default implementation is provided where a key has no 'aliases'.
 *
 * @author Kris De Volder
 */
public interface KeyAliases {

	public static final KeyAliases NONE = new KeyAliases() {
		@Override
		public Iterable<String> getKeyAliases(String base) {
			return Collections.emptyList();
		}

		public String toString() { return "KeyAliasses.NONE"; };
	};

	Iterable<String> getKeyAliases(String base);

}
