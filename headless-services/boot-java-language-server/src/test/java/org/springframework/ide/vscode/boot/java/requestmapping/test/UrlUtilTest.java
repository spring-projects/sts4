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
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.requestmapping.UrlUtil;

/**
 * @author Martin Lippert
 */
public class UrlUtilTest {

	@Test
	public void testSplitPathWithoutDuplicate() {
		String path = "/superpath";
		String[] splitPath = UrlUtil.splitPath(path);
		assertEquals(1, splitPath.length);
		assertEquals("/superpath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCaseWithEmptyOr() {
		String path = "/superpath/mypath || ";
		String[] splitPath = UrlUtil.splitPath(path);
		assertEquals(1, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
	}

	@Test
	public void testSplitPathSimpleCase() {
		String path = "/superpath/mypath || mypath.json";
		String[] splitPath = UrlUtil.splitPath(path);
		assertEquals(2, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/superpath/mypath.json", splitPath[1]);
	}

	@Test
	public void testSplitPathMultipleCases() {
		String path = "/superpath/mypath || mypath.json || somethingelse.what";
		String[] splitPath = UrlUtil.splitPath(path);
		assertEquals(3, splitPath.length);
		assertEquals("/superpath/mypath", splitPath[0]);
		assertEquals("/superpath/mypath.json", splitPath[1]);
		assertEquals("/superpath/somethingelse.what", splitPath[2]);
	}

}
