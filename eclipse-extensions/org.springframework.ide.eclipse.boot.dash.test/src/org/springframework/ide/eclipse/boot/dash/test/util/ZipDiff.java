/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.util;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility for comparing a given 'expected' zip file's contents with
 * a 'actual' zip contents.
 * <p>
 * Two zips are considered equal if they have the same files / folders
 * in their table-of contents and each of the files has identical contents.
 * <p>
 * This differ doesn't actually compare two files directly but rather computes
 * a hash of the file contents instead and compares the hashes. So...
 * their is a small chance that a difference may not be detected.
 *
 * @author Kris De Volder
 */
public class ZipDiff {

	final Map<String, Long> expectedHashes;

	public ZipDiff(InputStream expectedZipData) throws IOException {
		expectedHashes = computeHashes(expectedZipData);
	}

	public Map<String, Long> computeHashes(InputStream expectedZipData) throws IOException {
		Map<String, Long> hashes = new HashMap<>();
		ZipInputStream zip = new ZipInputStream(expectedZipData);
		ZipEntry ze;
		byte[] buffer = new byte[1024];
		while (null != (ze = zip.getNextEntry())) {
			String path = ze.getName();
			if (ze.isDirectory()) {
				hashes.put(path, -1L);
			} else {
				CRC32 hasher = new CRC32();
				int len;
				while ((len = zip.read(buffer)) > 0) {
					hasher.update(buffer, 0, len);
				}
				hashes.put(path, hasher.getValue());
			}
		}
		return hashes;
	}

	/**
	 * Read data from 'actualBits', interpret is as a ZipInputStream,
	 * and compare its contents to the expected contents.
	 * <p>
	 * Throws an exception when a difference is found.
	 */
	public void assertEqual(InputStream actualBits) throws IOException {
		Map<String, Long> actualHashes = computeHashes(actualBits);

		//Check that everything that is in 'actualBits' was as expected
		for (Entry<String, Long> actual : actualHashes.entrySet()) {
			if (!expectedHashes.containsKey(actual.getKey())) {
				fail("ZipEntry found but not expected: "+actual.getKey());
			}
			long expectedHash = expectedHashes.get(actual.getKey());
			long actualHash = actual.getValue();
			if (expectedHash!=actualHash) {
				fail("ZipEntry with different hashes: "+actual.getKey()+" "+expectedHash+"!="+actualHash);
			}
		}

		//Check that there's nothing that was expected but was missing from 'actual'
		for (String expected : expectedHashes.keySet()) {
			if (!actualHashes.containsKey(expected)) {
				fail("ZipEntry expected but not found: "+expected);
			}
		}
	}



}
