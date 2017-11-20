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
package org.springframework.ide.vscode.boot.java.utils.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.maven.MavenCore;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * CU Cache tests
 *
 * @author Alex Boyko
 *
 */
public class CompilationUnitCacheTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;

	@Before
	public void setup() throws Exception {
		harness = BootLanguageServerHarness.builder().build();
	}

	@Test
	public void cu_cached() throws Exception {
		harness.intialize(null);

		TextDocument doc = new TextDocument(harness.createTempUri(), LanguageId.JAVA, 0, "package my.package\n" +
				"\n" +
				"public class SomeClass {\n" +
				"\n" +
				"}\n");
		CompilationUnit cu = getCompilationUnit(doc);
		assertNotNull(cu);

		CompilationUnit cuAnother = getCompilationUnit(doc);
		assertTrue(cu == cuAnother);
	}

	private CompilationUnit getCompilationUnit(TextDocument doc) {
		return harness.getServer().getCompilationUnitCache().withCompilationUnit(doc, cu -> cu);
	}

	@Test
	public void cu_cache_invalidated_by_doc_change() throws Exception {
		harness.intialize(null);

		TextDocument doc = new TextDocument(harness.createTempUri(), LanguageId.JAVA, 0, "package my.package\n" +
				"\n" +
				"public class SomeClass {\n" +
				"\n" +
				"}\n");

		harness.newEditorFromFileUri(doc.getUri(), doc.getLanguageId());
		CompilationUnit cu = getCompilationUnit(doc);
		assertNotNull(cu);

		harness.changeDocument(doc.getUri(), 0, 0, "     ");
		CompilationUnit cuAnother = getCompilationUnit(doc);
		assertNotNull(cuAnother);
		assertFalse(cu == cuAnother);

		CompilationUnit cuYetAnother = getCompilationUnit(doc);
		assertTrue(cuAnother == cuYetAnother);
	}

	@Test
	public void cu_cache_invalidated_by_doc_close() throws Exception {
		harness.intialize(null);

		TextDocument doc = new TextDocument(harness.createTempUri(), LanguageId.JAVA, 0, "package my.package\n" +
				"\n" +
				"public class SomeClass {\n" +
				"\n" +
				"}\n");

		harness.newEditorFromFileUri(doc.getUri(), doc.getLanguageId());
		CompilationUnit cu = getCompilationUnit(doc);
		assertNotNull(cu);

		harness.closeDocument(doc.getId());
		CompilationUnit cuAnother = getCompilationUnit(doc);
		assertNotNull(cuAnother);
		assertFalse(cu == cuAnother);

		CompilationUnit cuYetAnother = getCompilationUnit(doc);
		assertTrue(cuAnother == cuYetAnother);
	}

	@Test
	public void cu_cache_invalidated_by_project_change() throws Exception {
		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/HelloWorldController.java";

		harness.intialize(directory);

		URI fileUri = new URI(docUri);
		Path path = Paths.get(fileUri);
		String content = new String(Files.readAllBytes(path));

		TextDocument document = new TextDocument(docUri, LanguageId.JAVA, 0, content);

		CompilationUnit cu = getCompilationUnit(document);
		assertNotNull(cu);
		CompilationUnit cuAnother = getCompilationUnit(document);
		assertTrue(cu == cuAnother);

		harness.changeFile(directory.toPath().resolve(MavenCore.POM_XML).toUri().toString());
		cuAnother = getCompilationUnit(document);
		assertNotNull(cuAnother);
		assertFalse(cu == cuAnother);
	}

	@Test
	public void cu_cache_invalidated_by_project_deletion() throws Exception {
		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/HelloWorldController.java";

		harness.intialize(directory);

		URI fileUri = new URI(docUri);
		Path path = Paths.get(fileUri);
		String content = new String(Files.readAllBytes(path));

		TextDocument document = new TextDocument(docUri, LanguageId.JAVA, 0, content);

		CompilationUnit cu = getCompilationUnit(document);
		assertNotNull(cu);
		CompilationUnit cuAnother = getCompilationUnit(document);
		assertTrue(cu == cuAnother);

		harness.deleteFile(directory.toPath().resolve(MavenCore.POM_XML).toUri().toString());
		cuAnother = getCompilationUnit(document);
		assertNotNull(cuAnother);
		assertFalse(cu == cuAnother);
	}
}
