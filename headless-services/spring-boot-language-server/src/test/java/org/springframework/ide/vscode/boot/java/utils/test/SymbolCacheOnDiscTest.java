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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxElementsInformation;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheKey;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheOnDisc;
import org.springframework.ide.vscode.commons.util.UriUtil;

public class SymbolCacheOnDiscTest {

	private Path tempDir;
	private SymbolCacheOnDisc cache;

	@Before
	public void setup() throws Exception {
		tempDir = Files.createTempDirectory("cachetest");
		cache = new SymbolCacheOnDisc(tempDir.toFile());
	}

	@After
	public void deleteTempDir() throws Exception {
		FileUtils.deleteDirectory(tempDir.toFile());
	}

	@Test
	public void testEmptyCache() throws Exception {
		CachedSymbol[] result = cache.retrieve(new SymbolCacheKey("something", "0"), new String[0]);
		assertNull(result);
	}

	@Test
	public void testSimpleValidCache() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toString(), file2.toString(), file3.toString()};

		List<CachedSymbol> generatedSymbols = new ArrayList<>();
		SymbolInformation symbol = new SymbolInformation("symbol1", SymbolKind.Field, new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, null);
		generatedSymbols.add(new CachedSymbol("", timeFile1.toMillis(), enhancedSymbol));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(1, cachedSymbols.length);

		assertEquals("symbol1", cachedSymbols[0].getEnhancedSymbol().getSymbol().getName());
		assertEquals(SymbolKind.Field, cachedSymbols[0].getEnhancedSymbol().getSymbol().getKind());
		assertEquals(new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))), cachedSymbols[0].getEnhancedSymbol().getSymbol().getLocation());
		assertNull(cachedSymbols[0].getEnhancedSymbol().getAdditionalInformation());
	}

	@Test
	public void testDifferentCacheKey() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toString(), file2.toString(), file3.toString()};

		List<CachedSymbol> generatedSymbols = new ArrayList<>();
		SymbolInformation symbol = new SymbolInformation("symbol1", SymbolKind.Field, new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, null);
		generatedSymbols.add(new CachedSymbol("", timeFile1.toMillis(), enhancedSymbol));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("otherkey", "1"), files);
		assertNull(cachedSymbols);
	}

	@Test
	public void testFileTouched() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toString(), file2.toString(), file3.toString()};

		List<CachedSymbol> generatedSymbols = new ArrayList<>();
		SymbolInformation symbol = new SymbolInformation("symbol1", SymbolKind.Field, new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, null);
		generatedSymbols.add(new CachedSymbol("", timeFile1.toMillis(), enhancedSymbol));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols);

		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 1000));

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNull(cachedSymbols);
	}

	@Test
	public void testMoreFiles() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		String[] files = {file1.toString(), file2.toString()};
		cache.store(new SymbolCacheKey("somekey", "1"), files, new ArrayList<>());

		String[] moreFiles = {file1.toString(), file2.toString(), file3.toString()};
		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), moreFiles);
		assertNull(cachedSymbols);
	}

	@Test
	public void testFewerFiles() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		String[] files = {file1.toString(), file2.toString(), file3.toString()};
		cache.store(new SymbolCacheKey("somekey", "1"), files, new ArrayList<>());

		String[] fewerFiles = {file1.toString(), file2.toString()};
		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), fewerFiles);
		assertNull(cachedSymbols);
	}

	@Test
	public void testDeleteOldCacheFileIfNewOneIsStored() throws Exception {
		SymbolCacheKey key1 = new SymbolCacheKey("somekey", "1");
		cache.store(key1, new String[0], new ArrayList<>());
		assertTrue(Files.exists(tempDir.resolve(Paths.get(key1.toString() + ".json"))));

		SymbolCacheKey key2 = new SymbolCacheKey("somekey", "2");
		cache.store(key2, new String[0], new ArrayList<>());
		assertTrue(Files.exists(tempDir.resolve(Paths.get(key2.toString() + ".json"))));
		assertFalse(Files.exists(tempDir.resolve(Paths.get(key1.toString() + ".json"))));
	}

	@Test
	public void testEnhancedInformationSubclasses() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Files.createFile(file1);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toString()};
		String doc1URI = UriUtil.toUri(file1.toFile()).toString();

		List<CachedSymbol> generatedSymbols = new ArrayList<>();

		SymbolInformation symbol = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		WebfluxElementsInformation addon = new WebfluxElementsInformation(new Range(new Position(4, 4), new Position(5, 5)), new Range(new Position(6, 6), new Position(7, 7)));
		EnhancedSymbolInformation enhancedSymbol = new EnhancedSymbolInformation(symbol, new SymbolAddOnInformation[] {addon});

		generatedSymbols.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(1, cachedSymbols.length);

		assertEquals("symbol1", cachedSymbols[0].getEnhancedSymbol().getSymbol().getName());
		assertEquals(SymbolKind.Field, cachedSymbols[0].getEnhancedSymbol().getSymbol().getKind());
		assertEquals(new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))), cachedSymbols[0].getEnhancedSymbol().getSymbol().getLocation());

		SymbolAddOnInformation[] retrievedAddOns = cachedSymbols[0].getEnhancedSymbol().getAdditionalInformation();
		assertNotNull(retrievedAddOns);
		assertEquals(1, retrievedAddOns.length);
		assertTrue(retrievedAddOns[0] instanceof WebfluxElementsInformation);

		Range[] ranges = ((WebfluxElementsInformation)retrievedAddOns[0]).getRanges();
		assertEquals(2, ranges.length);
		assertEquals(new Range(new Position(4, 4), new Position(5, 5)), ranges[0]);
		assertEquals(new Range(new Position(6, 6), new Position(7, 7)), ranges[1]);
	}

	@Ignore @Test
	public void testSymbolAddedToExistingFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");

		Files.createFile(file1);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toAbsolutePath().toString()};

		String doc1URI = UriUtil.toUri(file1.toFile()).toString();

		List<CachedSymbol> generatedSymbols1 = new ArrayList<>();
		SymbolInformation symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);
		generatedSymbols1.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol1));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);

		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Interface, new Location(doc1URI, new Range(new Position(5, 5), new Position(5, 10))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);

		generatedSymbols2.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, enhancedSymbol1));
		generatedSymbols2.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, enhancedSymbol2));

		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));
		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols2);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(2, cachedSymbols.length);
	}

	@Ignore @Test
	public void testSymbolRemovedFromExistingFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");

		Files.createFile(file1);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toAbsolutePath().toString()};

		String doc1URI = UriUtil.toUri(file1.toFile()).toString();

		List<CachedSymbol> generatedSymbols1 = new ArrayList<>();
		SymbolInformation symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);
		generatedSymbols1.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol1));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));

		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols2);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(0, cachedSymbols.length);
	}

	@Ignore @Test
	public void testSymbolAddedToNewFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");

		Files.createFile(file1);
		Files.createFile(file2);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		FileTime timeFile2 = Files.getLastModifiedTime(file2);
		String[] files = {file1.toString()};

		String doc1URI = UriUtil.toUri(file1.toFile()).toString();
		String doc2URI = UriUtil.toUri(file2.toFile()).toString();

		List<CachedSymbol> generatedSymbols1 = new ArrayList<>();
		SymbolInformation symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);
		generatedSymbols1.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol1));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Interface, new Location(doc2URI, new Range(new Position(5, 5), new Position(5, 10))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);

		generatedSymbols2.add(new CachedSymbol(doc2URI, timeFile2.toMillis(), enhancedSymbol2));

		cache.update(new SymbolCacheKey("somekey", "1"), file2.toString(), timeFile2.toMillis(), generatedSymbols2);

		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), new String[] {file1.toString(), file2.toString()});
		assertNotNull(cachedSymbols);
		assertEquals(2, cachedSymbols.length);
	}

	@Test
	public void testProjectDeleted() throws Exception {
		SymbolCacheKey key1 = new SymbolCacheKey("somekey", "1");
		cache.store(key1, new String[0], new ArrayList<>());
		assertTrue(Files.exists(tempDir.resolve(Paths.get(key1.toString() + ".json"))));

		cache.remove(key1);
		assertFalse(Files.exists(tempDir.resolve(Paths.get(key1.toString() + ".json"))));
	}

	@Test
	public void testFileDeleted() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");

		Files.createFile(file1);
		Files.createFile(file2);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		FileTime timeFile2 = Files.getLastModifiedTime(file2);
		String[] files = {file1.toAbsolutePath().toString(), file2.toAbsolutePath().toString()};

		String doc1URI = UriUtil.toUri(file1.toFile()).toString();
		String doc2URI = UriUtil.toUri(file2.toFile()).toString();

		List<CachedSymbol> generatedSymbols = new ArrayList<>();

		SymbolInformation symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);

		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Field, new Location(doc2URI, new Range(new Position(5, 10), new Position(5, 20))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);

		generatedSymbols.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol1));
		generatedSymbols.add(new CachedSymbol(doc2URI, timeFile2.toMillis(), enhancedSymbol2));

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols);
		cache.removeFile(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString());

		files = new String[] {file2.toAbsolutePath().toString()};
		CachedSymbol[] cachedSymbols = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(1, cachedSymbols.length);

		assertEquals("symbol2", cachedSymbols[0].getEnhancedSymbol().getSymbol().getName());
		assertEquals(SymbolKind.Field, cachedSymbols[0].getEnhancedSymbol().getSymbol().getKind());
		assertEquals(new Location(doc2URI, new Range(new Position(5, 10), new Position(5, 20))), cachedSymbols[0].getEnhancedSymbol().getSymbol().getLocation());
		assertNull(cachedSymbols[0].getEnhancedSymbol().getAdditionalInformation());
	}

}
