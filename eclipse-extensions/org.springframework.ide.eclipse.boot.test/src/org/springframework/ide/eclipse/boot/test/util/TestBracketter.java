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
package org.springframework.ide.eclipse.boot.test.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit 4 style test 'rule' that marks the beginning and ending of
 * a test execution by printing some recognizable markers/brackets
 * to System.out.
 * <p>
 * This is very helpful when trying to find a specific test's
 * output in the logs on bamboo builds which do not attach
 * individual test output to test results.
 *
 * @author Kris De Volder
 */
public class TestBracketter implements TestRule {

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			public void evaluate() throws Throwable {
				System.out.println(">>>> beg: "+description.getDisplayName());
				try {
					base.evaluate();
				} finally {
					System.out.println("<<<< end: "+description.getDisplayName());
				}
			}
		};
	}

}
