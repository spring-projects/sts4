/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springsource.ide.eclipse.commons.tests.util;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * A test to test ManagedTestSuite. We want to try to more gracefully handle
 * hanging tests by terminating just the offending test, not the whole build.
 * <p>
 * This test isn't part of the build and isn't supposed to "pass". It's just
 * something I wipped up to play around with ManagedTestSuite.
 * <p>
 * In Eclipse: run this test as JUnitPluginTest/SWTBotTest and the expected behaviour is to
 * see "testThatHangs" fail and the other tests pass (rather than the test run
 * hanging).
 * @author Kris De Volder
 */
public class TestTests {

	public static class HangingTest extends TestCase {

		public void testThatHangs() throws Exception {
			while (true) {
			}
		}

		public void testGoodOne() throws Exception {
		}

	}

	public static class GoodTest extends TestCase {
		public void testGoodA() throws Exception {
		}

		public void testGoodB() throws Exception {
		}
	}

	public static Test suite() {
		ManagedTestSuite suite = new ManagedTestSuite(TestTests.class.getName());
		suite.DELAY = 15000; // Make this go a little faster :-)
		suite.addTestSuite(HangingTest.class);
		suite.addTestSuite(GoodTest.class);
		return suite;
	}

}
