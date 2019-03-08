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
package org.springframework.ide.vscode.languageserver.testharness;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Represents a synchronization point in execution of some code.
 * <p>
 * This is used by testing code to be able to block a thread in
 * a manner controlled by the test flow.
 * <p>
 * This is a useful tool to allow creating test for
 * race conditions.
 *
 * @author Kris De Volder
 */
public interface SynchronizationPoint {

	/**
	 * Returns a future that resolves when the synchronization
	 * point is reached.
	 */
	Future<Void> reached();

	/**
	 * Unblocks the thread(s) that have reached the synchronization
	 * point.
	 */
	void unblock();

}
