/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.validation.generations.VersionValidationUtils;
import org.springframework.ide.vscode.commons.java.Version;

public class VersionValidationUtilsTest {
	
	final List<Version> releases = Arrays.asList(
			new Version(1,5,3,"RELEASE"),
			new Version(1,5,10,"RELEASE"),
			new Version(1,5,12,"RELEASE"),
			new Version(2,0,0,null),
			new Version(2,0,3,null),
			new Version(2,1,6,null),
			new Version(2,4,15,null),
			new Version(2,5,21,null),
			new Version(2,6,10,null),
			new Version(2,6,18,null),
			new Version(2,7,3,null),
			new Version(2,7,6,null),
			new Version(3,0,0,null)
		);
	
	@Test
	void testFindNewestPatchRelease() {
		assertEquals(new Version(1,5,12,"RELEASE"), VersionValidationUtils.getNewestPatchRelease(releases, new Version(1,5,3, "RELEASE")));
		assertEquals(new Version(1,4,3, null), VersionValidationUtils.getNewestPatchRelease(releases, new Version(1,4,3, null)));
		assertEquals(new Version(2,1,6,null), VersionValidationUtils.getNewestPatchRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(2,6,18,null), VersionValidationUtils.getNewestPatchRelease(releases, new Version(2,6,5, null)));
		assertEquals(new Version(3,0,0, null), VersionValidationUtils.getNewestPatchRelease(releases, new Version(3,0,0, null)));
		assertEquals(new Version(3,0,1, null), VersionValidationUtils.getNewestPatchRelease(releases, new Version(3,0,1, null)));
	}

	@Test
	void testGetNewerLatestPatchRelease() {
		assertEquals(new Version(1,5,12,"RELEASE"), VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(1,5,3, "RELEASE")));
		assertNull(VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(1,4,3, null)));
		assertEquals(new Version(2,1,6,null), VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(2,6,18,null), VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(2,6,5, null)));
		assertNull(VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(2,6,18, null)));
		assertNull(VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(3,0,0, null)));
		assertNull(VersionValidationUtils.getNewerLatestPatchRelease(releases, new Version(3,0,1, null)));
	}

	@Test
	void testFindNewestMinorRelease() {
		assertEquals(new Version(1,5,12,"RELEASE"), VersionValidationUtils.getNewestMinorRelease(releases, new Version(1,5,3, "RELEASE")));
		assertEquals(new Version(1,5,12,"RELEASE"), VersionValidationUtils.getNewestMinorRelease(releases, new Version(1,4,3, "RELEASE")));
		assertEquals(new Version(2,7,6,null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(2,7,6,null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(2,6,5, null)));
		assertEquals(new Version(2,7,6, null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(2,7,3, null)));
		assertEquals(new Version(2,7,6, null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(2,7,6, null)));
		assertEquals(new Version(3,0,0, null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(3,0,0, null)));
		assertEquals(new Version(3,0,1, null), VersionValidationUtils.getNewestMinorRelease(releases, new Version(3,0,1, null)));
	}

	@Test
	void testGetNewerLatestMinorRelease() {
		assertNull(VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(1,5,3, "RELEASE")));
		assertEquals(new Version(1,5,12,"RELEASE"), VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(1,4,3, "RELEASE")));
		assertEquals(new Version(2,7,6,null), VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(2,7,6,null), VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(2,6,5, null)));
		assertNull(VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(2,7,3, null)));
		assertNull(VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(2,7,6, null)));
		assertNull(VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(3,0,0, null)));
		assertNull(VersionValidationUtils.getNewerLatestMinorRelease(releases, new Version(3,0,1, null)));
	}

	@Test
	void testFindNewestMajorRelease() {
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(1,5,3, "RELEASE")));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(1,4,3, null)));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(2,6,5, null)));
		assertEquals(new Version(3,0,0, null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(3,0,0, null)));
		assertEquals(new Version(3,0,1, null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(3,0,1, null)));
		assertEquals(new Version(4,1,3, null), VersionValidationUtils.getNewestMajorRelease(releases, new Version(4,1,3, null)));
	}

	@Test
	void testGetNewerLatestMajorRelease() {
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(1,5,3, "RELEASE")));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(1,4,3, null)));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(2,1,3, null)));
		assertEquals(new Version(3,0,0,null), VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(2,6,5, null)));
		assertNull(VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(3,0,0, null)));
		assertNull(VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(3,0,1, null)));
		assertNull(VersionValidationUtils.getNewerLatestMajorRelease(releases, new Version(4,1,3, null)));
	}

}
