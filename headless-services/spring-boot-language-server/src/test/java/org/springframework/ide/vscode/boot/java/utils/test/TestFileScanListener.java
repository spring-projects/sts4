/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import org.springframework.ide.vscode.boot.java.utils.FileScanListener;
import org.springframework.ide.vscode.commons.util.UriUtil;

public class TestFileScanListener implements FileScanListener {
	public final List<String> scannedFiles = new ArrayList<String>();

	@Override
	public void fileScanned(String path) {
		scannedFiles.add(path);
	}

	public void assertScanned(String path) {
		assertTrue(scannedFiles.contains(path));
	}
	
	public void assertScanned(String path, int scanCount) {
		assertEquals(scanCount, scannedFiles.stream().filter(e -> e.equals(path)).count());
	}

	public void assertScannedUris(String... docUris) {
		List<String> docPaths = new ArrayList<>();
		for (String docUri : docUris) {
			docPaths.add(UriUtil.toFileString(docUri));
		}
		Collections.sort(docPaths);
		StringBuilder expected = new StringBuilder();
		for (String string : docPaths) {
			expected.append(string+"\n");
		}
		TreeSet<String> actualPaths = new TreeSet<>();
		for (String string : scannedFiles) {
			actualPaths.add(string);
		}
		StringBuilder actual = new StringBuilder();
		for (String string : actualPaths) {
			actual.append(string+"\n");
		}
		assertEquals(expected.toString(), actual.toString());
	}

	public void assertScannedUri(String docUri, int scanCount) {
		assertScanned(UriUtil.toFileString(docUri), scanCount);
	}

	public void reset() {
		scannedFiles.clear();
	}
}