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
package org.springframework.tooling.ls.eclipse.commons.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.tooling.ls.eclipse.commons.JRE;

public class JRETest {
	
	@Test public void parseVersion() throws Exception {
		assertEquals(1, JRE.parseVersion("1.8.123"));
		assertEquals(9, JRE.parseVersion("9.0.123"));
		assertEquals(10, JRE.parseVersion("10.0.123"));
		assertEquals(11, JRE.parseVersion("11-ea"));
	}

}
