/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons;

import java.util.concurrent.CompletableFuture;

public class Futures {
	
	public static <T> CompletableFuture<T> fail(Throwable e) {
		CompletableFuture<T> f = new CompletableFuture<T>();
		f.completeExceptionally(e);
		return f;
	}

}
