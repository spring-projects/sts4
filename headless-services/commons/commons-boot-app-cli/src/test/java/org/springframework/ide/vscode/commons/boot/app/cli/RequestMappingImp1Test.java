/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.boot.app.cli;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.RequestMappingImpl1;

/**
 * @author Martin Lippert
 */
public class RequestMappingImp1Test {

	@Test
	public void testSplitPathWithoutDuplicate() {
		RequestMappingImpl1 rm = new RequestMappingImpl1("/superpath", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(1, splitPath.length);
		assertEquals("/superpath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCaseWithEmptyOr() {
		RequestMappingImpl1 rm = new RequestMappingImpl1("/superpath/mypath || ", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(1, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCase() {
		RequestMappingImpl1 rm = new RequestMappingImpl1("/superpath/mypath || mypath.json", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(2, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/superpath/mypath.json", splitPath[1]);
	}

	@Test
	public void testSplitPathMultipleCases() {
		RequestMappingImpl1 rm = new RequestMappingImpl1("/superpath/mypath || mypath.json || somethingelse.what", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(3, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/superpath/mypath.json", splitPath[1]);
		assertEquals("/superpath/somethingelse.what", splitPath[2]);
	}

}
