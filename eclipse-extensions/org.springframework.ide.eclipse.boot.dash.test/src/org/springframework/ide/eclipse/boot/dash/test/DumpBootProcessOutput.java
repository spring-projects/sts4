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
package org.springframework.ide.eclipse.boot.dash.test;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.springframework.ide.eclipse.boot.launch.process.BootProcessFactory;

/**
 * JUnit 4 style test 'rule' that enables dumping of boot process
 * output onto System.out, for the duration of the test.
 *
 * @author Kris De Volder
 */
public class DumpBootProcessOutput implements TestRule {

	@Override
	public Statement apply(final Statement base, final Description description) {
		return new Statement() {
			public void evaluate() throws Throwable {
				BootProcessFactory.ENABLE_OUTPUT_DUMPING = true;
				try {
					base.evaluate();
				} finally {
					BootProcessFactory.ENABLE_OUTPUT_DUMPING = false;
				}
			}
		};
	}

}
