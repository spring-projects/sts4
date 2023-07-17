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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.index.cache.IndexCacheKey;

/**
 * @author Martin Lippert
 */
public class IndexCacheKeyTest {

    @Test
    void testCacheKey() {
        IndexCacheKey key = new IndexCacheKey("project", "indexer", "category", "version");
        IndexCacheKey keyEquals = new IndexCacheKey("project", "indexer", "category", "version");
        IndexCacheKey keyNotEquals = new IndexCacheKey("project", "indexer2", "category2", "version");

        assertEquals("project", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("version", key.getVersion());
        assertEquals("project-indexer-category-version", key.toString());
        
        assertEquals(key, keyEquals);
        assertEquals(key.hashCode(), keyEquals.hashCode());
        
        assertNotEquals(key, keyNotEquals);
        assertNotEquals(key.hashCode(), keyNotEquals.hashCode());
    }

    @Test
    void testCacheKeyParsingFromFileName() {
        IndexCacheKey key = IndexCacheKey.parse("project-indexer-category-version.json");
        assertEquals("project", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("project-name-with-separator-indexer-category-123ABC.json");
        assertEquals("project-name-with-separator", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("123ABC", key.getVersion());
    }

    @Test
    void testCacheKeyParsingWithoutFileExtension() {
        IndexCacheKey key = IndexCacheKey.parse("primary-indexer-category-version");
        assertEquals("primary", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("project-name-with-separator-indexer-category-123ABC");
        assertEquals("project-name-with-separator", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("123ABC", key.getVersion());
    }

    @Test
    void testCacheKeyParsingFromOldReleases() {
        IndexCacheKey key = IndexCacheKey.parse("project-with-separator-indexer--version");
        assertEquals("project-with-separator", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("", key.getCategory());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("project-with-separator-indexer--version.json");
        assertEquals("project-with-separator", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("", key.getCategory());
        assertEquals("version", key.getVersion());
    }
    
    @Test
    void testBrokenCacheFileNames() {
        IndexCacheKey key = IndexCacheKey.parse("version.json");
        assertEquals("", key.getProject());
        assertEquals("", key.getIndexer());
        assertEquals("", key.getCategory());
        assertEquals("version", key.getVersion());

        key = IndexCacheKey.parse("category-123ABC.json");
        assertEquals("", key.getProject());
        assertEquals("", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("123ABC", key.getVersion());
    	
        key = IndexCacheKey.parse("indexer-category-123ABC.json");
        assertEquals("", key.getProject());
        assertEquals("indexer", key.getIndexer());
        assertEquals("category", key.getCategory());
        assertEquals("123ABC", key.getVersion());

        key = IndexCacheKey.parse(".json");
        assertNull(key);
    }

}
