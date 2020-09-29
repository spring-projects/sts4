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
package org.springsource.ide.eclipse.commons.tests.util.swtbot;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Empty test case that runs super method for setup and tear down for general
 * cleanup.
 * @author Steffen Pingel
 */
public class UiCleanUpTestCase extends StsUiTestCase {

	public static Test suite() {
		TestSuite suite = new TestSuite(UiCleanUpTestCase.class.getName());
		suite.addTestSuite(UiCleanUpTestCase.class);
		return suite;
	}

	public void testVoid() {
		// do nothing
	}

}
