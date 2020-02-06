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
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.java.utils.test.TestFileScanListener;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness.CustomizableProjectContent;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class RequestMappingDependentConstantChangedTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;

	ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private MavenJavaProject project;
	private Path directory;
	
	@Before
	public void setup() throws Exception {
		harness.intialize(null);

		project = (MavenJavaProject) projects.mavenProject("test-request-mapping-symbols", false, new ProjectsHarness.ProjectCustomizer() {
			@Override
			public void customize(CustomizableProjectContent projectContent) throws Exception {
				//dummy (forces every test to use a new copy of the project, we do this because project
				//files are being mutated!
			}
		});
		directory = new File(project.getLocationUri()).toPath();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(project.getLocationUri().toString())).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testSimpleRequestMappingSymbolFromConstantInDifferentClass() throws Exception {
		String docUri = directory.resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
		String constantsUri = directory.resolve("src/main/java/org/test/Constants.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertSymbol(docUri, "@/path/from/constant", "@RequestMapping(Constants.REQUEST_MAPPING_PATH)");

		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);
		
		replaceInFile(constantsUri, "path/from/constant", "/changed-path");
		indexer.updateDocument(constantsUri, null, "triggered by test code").get();
		
		fileScanListener.assertScannedUris(constantsUri, docUri);
		fileScanListener.assertScannedUri(constantsUri, 1);
		fileScanListener.assertScannedUri(docUri, 1);
		
		symbols = indexer.getSymbols(docUri);
		assertSymbolCount(1, symbols);
		assertSymbol(docUri, "@/changed-path", "@RequestMapping(Constants.REQUEST_MAPPING_PATH)");
	}
	
	@Test
	public void testSimpleRequestMappingSymbolFromConstantInDifferentClassViaMultipleFilesUpdate() throws Exception {
		String docUri = directory.resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
		String constantsUri = directory.resolve("src/main/java/org/test/Constants.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertSymbol(docUri, "@/path/from/constant", "@RequestMapping(Constants.REQUEST_MAPPING_PATH)");

		TestFileScanListener fileScanListener = new TestFileScanListener();
		indexer.getJavaIndexer().setFileScanListener(fileScanListener);
		
		replaceInFile(constantsUri, "path/from/constant", "/changed-path");
		indexer.updateDocuments(new String[] {constantsUri}, "triggered by test code").get();

		fileScanListener.assertScannedUris(constantsUri, docUri);
		fileScanListener.assertScannedUri(constantsUri, 1);
		fileScanListener.assertScannedUri(docUri, 1);
		
		symbols = indexer.getSymbols(docUri);
		assertSymbolCount(1, symbols);
		assertSymbol(docUri, "@/changed-path", "@RequestMapping(Constants.REQUEST_MAPPING_PATH)");
	}
	
	@Test
	public void testRequestMappingSymbolFromConstantChained() throws Exception {
		String docUri = directory.resolve("src/main/java/org/test/ChainedRequestMappingPathOverMultipleClasses.java").toUri().toString();
		String chainConstantsUri_2 = directory.resolve("src/main/java/org/test/ChainElement2.java").toUri().toString();

		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(1, symbols.size());
		assertSymbol(docUri, "@/path/from/chain", "@RequestMapping(ChainElement1.MAPPING_PATH_1)");

		replaceInFile(chainConstantsUri_2, "path/from/chain", "/changed-path");
		indexer.updateDocument(chainConstantsUri_2, null, "triggered by test code").get();

		symbols = indexer.getSymbols(docUri);
		assertSymbolCount(1, symbols);
		assertSymbol(docUri, "@/path/from/chain", "@RequestMapping(ChainElement1.MAPPING_PATH_1)");
		
		// You would expect here that the symbol got updated from "path/from/chain" to the changed value "/changed-path",
		// but the mechanism doesn't know anything about this chained dependendy. This is a limitation of the current
		// implementation, since the AST has no idea about the chain, therefore we are only aware of the first
		// element in this chained dependency, which comes from ChainElement1.java
	}
	
	@Test
	public void testCyclicalDependency() throws Exception {
		//cyclical dependency between two files (ping refers pong and vice versa)
		
		String pingUri = directory.resolve("src/main/java/org/test/PingConstantRequestMapping.java").toUri().toString();
		String pongUri = directory.resolve("src/main/java/org/test/PongConstantRequestMapping.java").toUri().toString();

		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pingUri);
			for (SymbolInformation s : symbols) {
				System.out.println(s.getName());
			}
			assertSymbolCount(1, symbols);
			assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
		}
		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pongUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pongUri, "@/ping -- GET", "@GetMapping(PingConstantRequestMapping.PING)");
		}

		replaceInFile(pingUri, "/ping", "/changed");
		indexer.updateDocument(pingUri, null, "triggered by test code").get();

		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pingUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
		}
		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pongUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pongUri, "@/changed -- GET", "@GetMapping(PingConstantRequestMapping.PING)");
		}
	}

	@Test
	public void testCyclicalDependencyViaMultipleFilesUpdate() throws Exception {
		//cyclical dependency between two files (ping refers pong and vice versa)
		
		String pingUri = directory.resolve("src/main/java/org/test/PingConstantRequestMapping.java").toUri().toString();
		String pongUri = directory.resolve("src/main/java/org/test/PongConstantRequestMapping.java").toUri().toString();

		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pingUri);
			for (SymbolInformation s : symbols) {
				System.out.println(s.getName());
			}
			assertSymbolCount(1, symbols);
			assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
		}
		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pongUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pongUri, "@/ping -- GET", "@GetMapping(PingConstantRequestMapping.PING)");
		}

		replaceInFile(pingUri, "/ping", "/changed");
		indexer.updateDocuments(new String[] {pingUri}, "triggered by test code").get();

		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pingUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
		}
		{
			List<? extends SymbolInformation> symbols = indexer.getSymbols(pongUri);
			assertSymbolCount(1, symbols);
			assertSymbol(pongUri, "@/changed -- GET", "@GetMapping(PingConstantRequestMapping.PING)");
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void assertSymbolCount(int expectedCount, List<? extends SymbolInformation> symbols) {
		if (symbols.size()!=expectedCount) {
			StringBuilder found = new StringBuilder();
			for (SymbolInformation s : symbols) {
				found.append(s.getName());
				found.append("\n");
			}
			fail("Expected "+expectedCount+" symbols but found "+symbols.size()+":\n"+found);
		}
	}

	private void assertSymbol(String docUri, String name, String coveredText) throws Exception {
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		Optional<? extends SymbolInformation> maybeSymbol = symbols.stream().filter(s -> name.equals(s.getName())).findFirst();
		assertTrue(maybeSymbol.isPresent());
		
		TextDocument doc = new TextDocument(docUri, LanguageId.JAVA);
		doc.setText(FileUtils.readFileToString(UriUtil.toFile(docUri)));
		
		SymbolInformation symbol = maybeSymbol.get();
		Location loc = symbol.getLocation();
		assertEquals(docUri, loc.getUri());
		int start = doc.toOffset(loc.getRange().getStart());
		int end = doc.toOffset(loc.getRange().getEnd());
		String actualCoveredText = doc.textBetween(start, end);
		assertEquals(coveredText, actualCoveredText);
	}
	
	public void replaceInFile(String docUri, String find, String replace) throws Exception {
		File target = UriUtil.toFile(docUri);
		String oldContent = FileUtils.readFileToString(target, "UTF8");
		assertTrue(oldContent.contains(find));
		String newContent = oldContent.replace(find, replace);
		FileUtils.write(target, newContent, "UTF8");
	}
}
