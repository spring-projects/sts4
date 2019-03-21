/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
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
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheKey;

/**
 * @author Martin Lippert
 */
public class SymbolCacheKeyTest {

	@Test
	public void testCacheKey() {
		SymbolCacheKey key = new SymbolCacheKey("primary", "version");

		assertEquals("primary", key.getPrimaryIdentifier());
		assertEquals("version", key.getVersion());
		assertEquals("primary-version", key.toString());
	}

	@Test
	public void testCacheKeyParsingFromFileName() {
		SymbolCacheKey key = SymbolCacheKey.parse("primary-version.json");
		assertEquals("primary", key.getPrimaryIdentifier());
		assertEquals("version", key.getVersion());

		key = SymbolCacheKey.parse("primary-name-with-separator-123ABC.json");
		assertEquals("primary-name-with-separator", key.getPrimaryIdentifier());
		assertEquals("123ABC", key.getVersion());
	}

	@Test
	public void testCacheKeyParsingWithoutFileExtension() {
		SymbolCacheKey key = SymbolCacheKey.parse("primary-version");
		assertEquals("primary", key.getPrimaryIdentifier());
		assertEquals("version", key.getVersion());

		key = SymbolCacheKey.parse("primary-name-with-separator-123ABC");
		assertEquals("primary-name-with-separator", key.getPrimaryIdentifier());
		assertEquals("123ABC", key.getVersion());
	}

	@Test
	public void testCacheKeyEquals() {
		SymbolCacheKey key1 = new SymbolCacheKey("primary", "1");
		SymbolCacheKey key2 = new SymbolCacheKey("primary", "1");

		SymbolCacheKey key3 = new SymbolCacheKey("primary", "2");
		SymbolCacheKey key4 = new SymbolCacheKey("secondary", "1");

		assertEquals(key1, key1);
		assertEquals(key2, key2);
		assertEquals(key3, key3);
		assertEquals(key4, key4);

		assertEquals(key1, key2);

		assertNotEquals(key1, key3);
		assertNotEquals(key2, key3);
		assertNotEquals(key3, key4);
		assertNotEquals(key1, key4);
		assertNotEquals(key4, key1);
		assertNotEquals(key4, key2);
	}

}
