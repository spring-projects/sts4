/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.util;

import java.util.stream.Stream;

public class Streams {

	/**
	 * Like java.util.Stream.of but returns Stream.empty if the element is null
	 */
	public static <T> Stream<T> fromNullable(T e) {
		return e==null ? Stream.empty() : Stream.of(e);
	}

}
