/*******************************************************************************
 * Copyright (c) 2019, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index.cache.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.index.cache.IndexCacheKey;

/**
 * @author Martin Lippert
 */
public class IndexCacheKeyTest {

    @Test
    void testCacheKey() {
        IndexCacheKey key = new IndexCacheKey("primary", "version");

        assertEquals("primary", key.getPrimaryIdentifier());
        assertEquals("version", key.getVersion());
        assertEquals("primary-version", key.toString());
    }

    @Test
    void testCacheKeyParsingFromFileName() {
        IndexCacheKey key = IndexCacheKey.parse("primary-version.json");
        assertEquals("primary", key.getPrimaryIdentifier());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("primary-name-with-separator-123ABC.json");
        assertEquals("primary-name-with-separator", key.getPrimaryIdentifier());
        assertEquals("123ABC", key.getVersion());
    }

    @Test
    void testCacheKeyParsingWithoutFileExtension() {
        IndexCacheKey key = IndexCacheKey.parse("primary-version");
        assertEquals("primary", key.getPrimaryIdentifier());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("primary-name-with-separator-123ABC");
        assertEquals("primary-name-with-separator", key.getPrimaryIdentifier());
        assertEquals("123ABC", key.getVersion());
    }

    @Test
    void testCacheKeyEquals() {
        IndexCacheKey key1 = new IndexCacheKey("primary", "1");
        IndexCacheKey key2 = new IndexCacheKey("primary", "1");

        IndexCacheKey key3 = new IndexCacheKey("primary", "2");
        IndexCacheKey key4 = new IndexCacheKey("secondary", "1");

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
