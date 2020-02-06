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
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxElementsInformation;
import org.springframework.ide.vscode.boot.java.utils.CachedSymbol;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheKey;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheOnDisc;
import org.springframework.ide.vscode.commons.util.UriUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

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
		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("something", "0"), new String[0]);
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, ImmutableMultimap.of(
				file1.toString(), "file1dep1",
				file2.toString(), "file2dep1",
				file2.toString(), "file2dep2"
		));

		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		
		CachedSymbol[] cachedSymbols = result.getLeft();
		assertNotNull(cachedSymbols);
		assertEquals(1, cachedSymbols.length);

		assertEquals("symbol1", cachedSymbols[0].getEnhancedSymbol().getSymbol().getName());
		assertEquals(SymbolKind.Field, cachedSymbols[0].getEnhancedSymbol().getSymbol().getKind());
		assertEquals(new Location("docURI", new Range(new Position(3, 10), new Position(3, 20))), cachedSymbols[0].getEnhancedSymbol().getSymbol().getLocation());
		assertNull(cachedSymbols[0].getEnhancedSymbol().getAdditionalInformation());
		
		Multimap<String, String> dependencies = result.getRight();
		assertEquals(2, dependencies.keySet().size());
		assertEquals(dependencies.get(file1.toString()), ImmutableSet.of("file1dep1"));
		assertEquals(dependencies.get(file2.toString()), ImmutableSet.of("file2dep1", "file2dep2"));
		
		assertEquals(timeFile1.toMillis(), cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), file1.toString()));
		assertEquals(0, cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), "random-non-existing-file"));
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, null);

		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("otherkey", "1"), files);
		assertNull(result);
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, ImmutableMultimap.of(
				file1.toString(), "file1dep",
				file2.toString(), "file2dep"
		));

		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 1000));
		
		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNull(result);
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
		cache.store(new SymbolCacheKey("somekey", "1"), files, new ArrayList<>(), null);

		String[] moreFiles = {file1.toString(), file2.toString(), file3.toString()};
		assertNull(cache.retrieve(new SymbolCacheKey("somekey", "1"), moreFiles));
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
		cache.store(new SymbolCacheKey("somekey", "1"), files, new ArrayList<>(), null);

		String[] fewerFiles = {file1.toString(), file2.toString()};
		assertNull(cache.retrieve(new SymbolCacheKey("somekey", "1"), fewerFiles));
	}

	@Test
	public void testDeleteOldCacheFileIfNewOneIsStored() throws Exception {
		SymbolCacheKey key1 = new SymbolCacheKey("somekey", "1");
		cache.store(key1, new String[0], new ArrayList<>(), null);
		assertTrue(Files.exists(tempDir.resolve(Paths.get(key1.toString() + ".json"))));

		SymbolCacheKey key2 = new SymbolCacheKey("somekey", "2");
		cache.store(key2, new String[0], new ArrayList<>(), null);
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, null);

		CachedSymbol[] cachedSymbols = cache.retrieveSymbols(new SymbolCacheKey("somekey", "1"), files);
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

	@Test
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1, null);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);

		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Interface, new Location(doc1URI, new Range(new Position(5, 5), new Position(5, 10))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);

		generatedSymbols2.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, enhancedSymbol1));
		generatedSymbols2.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, enhancedSymbol2));

		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));
		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols2, null);

		CachedSymbol[] cachedSymbols = cache.retrieveSymbols(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(2, cachedSymbols.length);
		
		assertEquals(timeFile1.toMillis() + 2000, cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), file1.toString()));
	}

	@Test
	public void testSymbolsAddedToMultipleFiles() throws Exception {
		
		// create 3 files with one symbol each
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");
		Path file3 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile3");

		Files.createFile(file1);
		Files.createFile(file2);
		Files.createFile(file3);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		FileTime timeFile2 = Files.getLastModifiedTime(file2);
		FileTime timeFile3 = Files.getLastModifiedTime(file3);

		String[] files = {file1.toAbsolutePath().toString(), file2.toAbsolutePath().toString(), file3.toAbsolutePath().toString()};

		String doc1URI = UriUtil.toUri(file1.toFile()).toString();
		String doc2URI = UriUtil.toUri(file2.toFile()).toString();
		String doc3URI = UriUtil.toUri(file3.toFile()).toString();

		List<CachedSymbol> generatedSymbols = new ArrayList<>();
		SymbolInformation symbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol1 = new EnhancedSymbolInformation(symbol1, null);
		generatedSymbols.add(new CachedSymbol(doc1URI, timeFile1.toMillis(), enhancedSymbol1));

		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Field, new Location(doc2URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);
		generatedSymbols.add(new CachedSymbol(doc2URI, timeFile2.toMillis(), enhancedSymbol2));

		SymbolInformation symbol3 = new SymbolInformation("symbol3", SymbolKind.Field, new Location(doc3URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation enhancedSymbol3 = new EnhancedSymbolInformation(symbol3, null);
		generatedSymbols.add(new CachedSymbol(doc3URI, timeFile3.toMillis(), enhancedSymbol3));

		// store original version of the symbols to the cache 
		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, null);


		// create updated and new symbols
		List<CachedSymbol> updatedSymbols = new ArrayList<>();
		
		SymbolInformation updatedSymbol1 = new SymbolInformation("symbol1", SymbolKind.Field, new Location(doc1URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation updatedEnhancedSymbol1 = new EnhancedSymbolInformation(updatedSymbol1, null);

		SymbolInformation newSymbol1 = new SymbolInformation("symbol1-new", SymbolKind.Interface, new Location(doc1URI, new Range(new Position(5, 5), new Position(5, 10))));
		EnhancedSymbolInformation newEnhancedSymbol1 = new EnhancedSymbolInformation(newSymbol1, null);

		updatedSymbols.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, updatedEnhancedSymbol1));
		updatedSymbols.add(new CachedSymbol(doc1URI, timeFile1.toMillis() + 2000, newEnhancedSymbol1));
		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));

		SymbolInformation updatedSymbol2 = new SymbolInformation("symbol2-updated", SymbolKind.Field, new Location(doc2URI, new Range(new Position(3, 10), new Position(3, 20))));
		EnhancedSymbolInformation updatedEnhancedSymbol2 = new EnhancedSymbolInformation(updatedSymbol2, null);
		updatedSymbols.add(new CachedSymbol(doc2URI, timeFile2.toMillis() + 3000, updatedEnhancedSymbol2));
		assertTrue(file2.toFile().setLastModified(timeFile2.toMillis() + 3000));
		
		String[] updatedFiles = new String[] {file1.toAbsolutePath().toString(), file2.toAbsolutePath().toString()};
		long[] updatedModificationTimestamps = new long[] {timeFile1.toMillis() + 2000, timeFile2.toMillis() + 3000};
		
		// update multiple files in the cache
		cache.update(new SymbolCacheKey("somekey", "1"), updatedFiles, updatedModificationTimestamps, updatedSymbols, null);

		// double check whether all changes got stored and retrieved correctly
		CachedSymbol[] cachedSymbols = cache.retrieveSymbols(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(4, cachedSymbols.length);
		
		assertSymbol(updatedEnhancedSymbol1, cachedSymbols);
		assertSymbol(newEnhancedSymbol1, cachedSymbols);
		assertSymbol(updatedEnhancedSymbol2, cachedSymbols);
		assertSymbol(enhancedSymbol3, cachedSymbols);

		assertEquals(timeFile1.toMillis() + 2000, cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), file1.toString()));
		assertEquals(timeFile2.toMillis() + 3000, cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), file2.toString()));
		assertEquals(timeFile3.toMillis(), cache.getModificationTimestamp(new SymbolCacheKey("somekey", "1"), file3.toString()));
	}

	private void assertSymbol(EnhancedSymbolInformation enhancedSymbol, CachedSymbol[] cachedSymbols) {
		for (CachedSymbol cachedSymbol : cachedSymbols) {
			SymbolInformation symbol = cachedSymbol.getEnhancedSymbol().getSymbol();
			
			if (symbol.toString().equals(enhancedSymbol.getSymbol().toString())) {
				return;
			}
		}
		
		
		Assert.fail("symbol not found: " + enhancedSymbol.getSymbol().toString());
	}

	@Test
	public void testDependencyAddedToExistingFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");

		Files.createFile(file1);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toAbsolutePath().toString()};

		List<CachedSymbol> generatedSymbols = ImmutableList.of();
		
		Multimap<String, String> dependencies = ImmutableMultimap.of(file1.toString(), "dep1");
		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, dependencies);

		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));
		Set<String> dependencies2 = ImmutableSet.of("dep1", "dep2");
		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols, dependencies2);

		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(result);
		assertEquals(ImmutableSet.of("dep1", "dep2"), result.getRight().get(file1.toString()));
	}

	@Test
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1, null);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));

		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols2, null);

		CachedSymbol[] cachedSymbols = cache.retrieveSymbols(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(cachedSymbols);
		assertEquals(0, cachedSymbols.length);
	}

	@Test
	public void testDependencyRemovedFromExistingFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");

		Files.createFile(file1);

		FileTime timeFile1 = Files.getLastModifiedTime(file1);
		String[] files = {file1.toAbsolutePath().toString()};

		List<CachedSymbol> generatedSymbols1 = ImmutableList.of();

		ImmutableMultimap<String, String> dependencies1 = ImmutableMultimap.of(
			file1.toString(), "dep1",
			file1.toString(), "dep2"
		);
		
		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1, dependencies1);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		assertTrue(file1.toFile().setLastModified(timeFile1.toMillis() + 2000));

		Set<String> dependencies2 = ImmutableSet.of("dep2");
		cache.update(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString(), timeFile1.toMillis() + 2000, generatedSymbols2, dependencies2);

		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		assertNotNull(result);
		assertEquals(ImmutableSet.of("dep2"), result.getRight().get(file1.toString()));
	}

	@Test
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

		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1, null);

		List<CachedSymbol> generatedSymbols2 = new ArrayList<>();
		SymbolInformation symbol2 = new SymbolInformation("symbol2", SymbolKind.Interface, new Location(doc2URI, new Range(new Position(5, 5), new Position(5, 10))));
		EnhancedSymbolInformation enhancedSymbol2 = new EnhancedSymbolInformation(symbol2, null);

		generatedSymbols2.add(new CachedSymbol(doc2URI, timeFile2.toMillis(), enhancedSymbol2));

		cache.update(new SymbolCacheKey("somekey", "1"), file2.toString(), timeFile2.toMillis(), generatedSymbols2, null);

		CachedSymbol[] cachedSymbols = cache.retrieveSymbols(new SymbolCacheKey("somekey", "1"), new String[] {file1.toString(), file2.toString()});
		assertNotNull(cachedSymbols);
		assertEquals(2, cachedSymbols.length);
	}

	@Test
	public void testDependencyAddedToNewFile() throws Exception {
		Path file1 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile1");
		Path file2 = Paths.get(tempDir.toAbsolutePath().toString(), "tempFile2");

		Files.createFile(file1);
		Files.createFile(file2);

		FileTime timeFile2 = Files.getLastModifiedTime(file2);
		String[] files = {file1.toString()};

		List<CachedSymbol> generatedSymbols1 = ImmutableList.of();
		Multimap<String, String> dependencies1 = ImmutableMultimap.of(
				file1.toString(), "dep1"
		);
		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols1, dependencies1);

		Set<String> dependencies2 = ImmutableSet.of("dep2");
		cache.update(new SymbolCacheKey("somekey", "1"), file2.toString(), timeFile2.toMillis(), generatedSymbols1, dependencies2);

		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), new String[] {file1.toString(), file2.toString()});
		assertNotNull(result);
		assertEquals(ImmutableSet.of("dep2"), result.getRight().get(file2.toString()));
		assertEquals(ImmutableSet.of("dep1"), result.getRight().get(file1.toString()));
	}

	@Test
	public void testProjectDeleted() throws Exception {
		SymbolCacheKey key1 = new SymbolCacheKey("somekey", "1");
		cache.store(key1, new String[0], new ArrayList<>(), null);
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

		Multimap<String, String> dependencies = ImmutableMultimap.of(
				file1.toString(), "dep1",
				file2.toString(), "dep2"
		);
		cache.store(new SymbolCacheKey("somekey", "1"), files, generatedSymbols, dependencies);
		cache.removeFile(new SymbolCacheKey("somekey", "1"), file1.toAbsolutePath().toString());

		files = new String[] {file2.toAbsolutePath().toString()};
		Pair<CachedSymbol[], Multimap<String, String>> result = cache.retrieve(new SymbolCacheKey("somekey", "1"), files);
		CachedSymbol[] cachedSymbols = result.getLeft();
		assertNotNull(result);
		assertEquals(1, cachedSymbols.length);

		assertEquals("symbol2", cachedSymbols[0].getEnhancedSymbol().getSymbol().getName());
		assertEquals(SymbolKind.Field, cachedSymbols[0].getEnhancedSymbol().getSymbol().getKind());
		assertEquals(new Location(doc2URI, new Range(new Position(5, 10), new Position(5, 20))), cachedSymbols[0].getEnhancedSymbol().getSymbol().getLocation());
		assertNull(cachedSymbols[0].getEnhancedSymbol().getAdditionalInformation());
		
		Multimap<String, String> cachedDependencies = result.getRight();
		assertEquals(ImmutableSet.of(), cachedDependencies.get(file1.toString()));
		assertEquals(ImmutableSet.of("dep2"), cachedDependencies.get(file2.toString()));
	}
}
