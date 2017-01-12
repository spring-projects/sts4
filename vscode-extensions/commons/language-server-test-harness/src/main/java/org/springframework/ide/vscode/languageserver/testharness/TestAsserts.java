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

package org.springframework.ide.vscode.languageserver.testharness;

import static org.junit.Assert.fail;

public class TestAsserts {

	public static void assertContains(String needle, String haystack) {
		if (haystack==null || !haystack.contains(needle)) {
			fail("Not found: "+needle+"\n in \n"+haystack);
		}
	}

}
