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
package org.springframework.ide.vscode.commons.util.test;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.ide.vscode.commons.util.ExceptionUtil;

/**
 * Utility for async condition checking, mostly intended for testing code.
 * <p>
 * Uses a polling loop to repeatedly evaluate some code that represents a condition
 * of some kind. The test passes as soon as the condition passes, or fails if 
 * the condition does not pass within a given time limit.
 */
public class ACondition {

	public interface Asserter {
		void doAsserts() throws Exception;
	}

	public static void waitFor(Duration timeout, Asserter asserter) throws Exception {
		long startTime = System.currentTimeMillis();
		long timeout_millis = timeout.toMillis();
		Throwable lastException = null;
		do {
			try {
				asserter.doAsserts();
				return;
			} catch (Throwable e) {
				lastException = e;
				if (System.currentTimeMillis()-startTime < timeout_millis) {
					Thread.sleep(300);
				}
			}
		} while (System.currentTimeMillis()-startTime < timeout_millis);
		throw ExceptionUtil.exception(lastException);
	}

	/**
	 * Retries fecthing a value until it succeeds without an error, or until timeout exceeded.
	 */
	public static <T> T waitForValue(Duration timeout, Callable<T> provider) throws Exception {
		AtomicReference<T> result = new AtomicReference<>(null);
		waitFor(timeout, () -> result.set(provider.call()));
		return result.get();
	}
	
}
