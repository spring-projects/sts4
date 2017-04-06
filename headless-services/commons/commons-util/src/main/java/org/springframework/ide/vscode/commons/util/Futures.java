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

package org.springframework.ide.vscode.commons.util;

import java.util.concurrent.CompletableFuture;

public class Futures {

	/**
	 * Depcrecated. Use {@link CompletableFuture}.completedFuture() instead.
	 */
	@Deprecated
	public static <T> CompletableFuture<T> of(T value) {
		return CompletableFuture.completedFuture(value);
	}
	
}
