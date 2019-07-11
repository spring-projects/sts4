/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RemoteSpringBootAppTest {
	
	@Test
	public void canCreateInstance() throws Exception {
		SpringBootApp instance = RemoteSpringBootApp.create("jmx:blah", "whatever.cfapps.io", "8888", "https", true);
		assertNotNull(instance);
		assertTrue(instance instanceof RemoteSpringBootApp);
	}

}
