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
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.AbstractRequestMapping;
import org.springframework.ide.vscode.commons.boot.app.cli.requestmappings.Boot1xRequestMapping;

/**
 * @author Martin Lippert
 */
public class Boot1xRequestMappingTest {

	@Test
	public void testSplitPathWithoutDuplicate() {
		AbstractRequestMapping rm = new Boot1xRequestMapping("/superpath", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(1, splitPath.length);
		assertEquals("/superpath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCaseWithEmptyOr() {
		AbstractRequestMapping rm = new Boot1xRequestMapping("/superpath/mypath || ", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(1, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCase() {
		AbstractRequestMapping rm = new Boot1xRequestMapping("{[/superpath/mypath || mypath.json]}", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(2, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/mypath.json", splitPath[1]);
	}

	@Test
	public void testSplitPathMultipleCases() {
		AbstractRequestMapping rm = new Boot1xRequestMapping("{[/superpath/mypath || mypath.json || somethingelse.what]}", null);
		String[] splitPath = rm.getSplitPath();
		assertEquals(3, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/mypath.json", splitPath[1]);
		assertEquals("/somethingelse.what", splitPath[2]);
	}

}
