/*******************************************************************************
 * Copyright (c) 2017, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.SymbolProviderTestConf;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.requestmapping.RequestMappingIndexElement;
import org.springframework.ide.vscode.boot.java.utils.SpringIndexerJavaDependencyTracker;
import org.springframework.ide.vscode.boot.java.utils.test.TestFileScanListener;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.commons.util.UriUtil;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.ImmutableSet;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class RequestMappingSymbolProviderTest {
	
	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;
	@Autowired private SpringMetamodelIndex springIndex;

	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);

		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-request-mapping-symbols/").toURI());
		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

    @Test
    void testSimpleRequestMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(2, symbols.size());
        assertTrue(containsSymbol(symbols, "@/greeting", docUri, 8, 1, 8, 29));
    }

    @Test
    void testRequestMappingIndexElements() throws Exception {
        Bean[] beans = springIndex.getBeansWithName("test-request-mapping-symbols", "simpleMappingClass");
        assertEquals(1, beans.length);
        
        List<SpringIndexElement> children = beans[0].getChildren();
        List<SpringIndexElement> mappingChildren = children.stream()
        	.filter(child -> child instanceof RequestMappingIndexElement)
        	.toList();
        
        assertEquals(1, mappingChildren.size());
        
        RequestMappingIndexElement mappingElement = (RequestMappingIndexElement) mappingChildren.get(0);
        assertEquals("/greeting", mappingElement.getPath());
    }

    @Test
    //TODO: Enable when JDT Core 3.41 or higher is adopted. See: https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3416
	@DisabledOnOs(OS.WINDOWS)
    void testSimpleRequestMappingSymbolFromConstantInDifferentClass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
        String constantsUri = directory.toPath().resolve("src/main/java/org/test/Constants.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/path/from/constant", docUri, 6, 1, 6, 48));

        //Verify whether dependency tracker logics works properly for this example.
        SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
        assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));

        TestFileScanListener fileScanListener = new TestFileScanListener();
        indexer.getJavaIndexer().setFileScanListener(fileScanListener);

        CompletableFuture<Void> updateFuture = indexer.updateDocument(constantsUri, FileUtils.readFileToString(UriUtil.toFile(constantsUri), Charset.defaultCharset()), "test triggered");
        updateFuture.get(5, TimeUnit.SECONDS);

        fileScanListener.assertScannedUris(constantsUri, docUri);
        fileScanListener.assertScannedUri(constantsUri, 1);
        fileScanListener.assertScannedUri(docUri, 1);
    }

    @Test
    //TODO: Enable when JDT Core 3.41 or higher is adopted. See: https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3416
	@DisabledOnOs(OS.WINDOWS)
    void testUpdateDocumentWithConstantFromDifferentClass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInDifferentClass.java").toUri().toString();
        String constantsUri = directory.toPath().resolve("src/main/java/org/test/Constants.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/path/from/constant", docUri, 6, 1, 6, 48));

        //Verify whether dependency tracker logics works properly for this example.
        SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
        assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));

        TestFileScanListener fileScanListener = new TestFileScanListener();
        indexer.getJavaIndexer().setFileScanListener(fileScanListener);

        CompletableFuture<Void> updateFuture = indexer.updateDocument(docUri, FileUtils.readFileToString(UriUtil.toFile(docUri), Charset.defaultCharset()), "test triggered");
        updateFuture.get(5, TimeUnit.SECONDS);

        assertEquals(ImmutableSet.of("Lorg/test/Constants;"), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));

        fileScanListener.assertScannedUris(docUri);
        fileScanListener.assertScannedUri(constantsUri, 0);
        fileScanListener.assertScannedUri(docUri, 1);
    }

    @Test
    //TODO: Enable when JDT Core 3.41 or higher is adopted. See: https://github.com/eclipse-jdt/eclipse.jdt.core/pull/3416
	@DisabledOnOs(OS.WINDOWS)
    void testCyclicalRequestMappingDependency() throws Exception {
        //Cyclical dependency:
        //file a => file b => file a
        //This has the potential to cause infinite loop.
        
        String pingUri = directory.toPath().resolve("src/main/java/org/test/PingConstantRequestMapping.java").toUri().toString();
        String pongUri = directory.toPath().resolve("src/main/java/org/test/PongConstantRequestMapping.java").toUri().toString();

        assertSymbol(pingUri, "@/pong -- GET", "@GetMapping(PongConstantRequestMapping.PONG)");
        assertSymbol(pongUri, "@/ping -- GET", "@GetMapping(PingConstantRequestMapping.PING)");

        TestFileScanListener fileScanListener = new TestFileScanListener();
        indexer.getJavaIndexer().setFileScanListener(fileScanListener);

        CompletableFuture<Void> updateFuture = indexer.updateDocument(pingUri, null, "test triggered");
        updateFuture.get(5, TimeUnit.SECONDS);

        fileScanListener.assertScannedUris(pingUri, pongUri);

        fileScanListener.reset();
        fileScanListener.assertScannedUris(/*none*/);

        CompletableFuture<Void> updateFuture2 = indexer.updateDocument(pongUri, null, "test triggered");
        updateFuture2.get(5, TimeUnit.SECONDS);

        fileScanListener.assertScannedUris(pingUri, pongUri);
    }

    @Test
    void testSimpleRequestMappingSymbolFromConstantInSameClass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantInSameClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/request/mapping/path/from/same/class/constant", docUri, 8, 1, 8, 52));

        SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
        assertEquals(ImmutableSet.of(), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
    }

    @Test
    void testSimpleRequestMappingSymbolFromConstantInBinaryType() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/SimpleMappingClassWithConstantFromBinaryType.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/(inferred)", docUri, 7, 1, 7, 53));

        SpringIndexerJavaDependencyTracker dt = indexer.getJavaIndexer().getDependencyTracker();
        assertEquals(ImmutableSet.of(), dt.getAllDependencies().get(UriUtil.toFileString(docUri)));
    }

    @Test
    void testParentRequestMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/ParentMappingClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/parent/greeting -- GET", docUri, 8, 1, 8, 51));
    }

    @Test
    void testEmptyPathWithParentRequestMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/ParentMappingClass2.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/parent2 -- GET,POST,DELETE", docUri, 11, 1, 11, 16));
    }

    @Test
    void testParentRequestMappingSymbolWithPathAttribute() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/ParentMappingClass3.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/parent3/greeting -- GET", docUri, 8, 1, 8, 51));
    }

    @Test
    void testMappingPathFromSuperclass() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/inheritance/SubclassWithMappingFromParent.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/superclasspath/", docUri, 6, 1, 6, 21));
    }

    @Test
    void testMappingPathFromSuperclassWithConstant() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/inheritance/SubclassWithMappingFromParentWithConstant.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/path/from/constant/", docUri, 6, 1, 6, 21));
    }

    @Test
    void testMappingPathFromSuperclassWithMethodsAndPathAttribute() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/inheritance/SubclassWithMappingFromParentWithMethods.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/superclasspath -- POST,PUT - Accept: testconsume - Content-Type: text/plain", docUri, 6, 1, 6, 16));
    }

    @Test
    void testMappingPathFromMultiLevelClassHierarchy() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/inheritance/SuperControllerLevel4.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(1, symbols.size());
        assertTrue(containsSymbol(symbols, "@/path-level2/final-subclass-path", docUri, 6, 1, 6, 39));
    }

    @Test
    void testMappingPathFromSuperInterfaceEvenIfSuperclassContainsMappingPath() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/inheritance/ControllerAsSubclassAndInterfaceHierarchy.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(2, symbols.size());
        assertTrue(containsSymbol(symbols, "@/superinterface-path/last-path-segment -- GET - Accept: testconsume - Content-Type: text/plain", docUri, 8, 1, 8, 33));
    }

    @Test
    void testMapoingIndexElementsWithDetails() throws Exception {
        Bean[] beans = springIndex.getBeansWithName("test-request-mapping-symbols", "controllerAsSubclassAndInterfaceHierarchy");
        assertEquals(1, beans.length);
        
        List<SpringIndexElement> children = beans[0].getChildren();
        List<SpringIndexElement> mappingChildren = children.stream()
        	.filter(child -> child instanceof RequestMappingIndexElement)
        	.toList();
        
        assertEquals(1, mappingChildren.size());
        
        RequestMappingIndexElement mappingElement = (RequestMappingIndexElement) mappingChildren.get(0);
        assertEquals("/superinterface-path/last-path-segment", mappingElement.getPath());

        assertEquals(1, mappingElement.getHttpMethods().length);
        assertEquals("GET", mappingElement.getHttpMethods()[0]);

        assertEquals(1, mappingElement.getAcceptTypes().length);
        assertEquals("testconsume", mappingElement.getAcceptTypes()[0]);

        assertEquals(1, mappingElement.getContentTypes().length);
        assertEquals("text/plain", mappingElement.getContentTypes()[0]);
    }

    @Test
    void testMultiRequestMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/MultiRequestMappingClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertEquals(2, symbols.size());
        assertTrue(containsSymbol(symbols, "@/hello1", docUri, 6, 1, 6, 44));
        assertTrue(containsSymbol(symbols, "@/hello2", docUri, 6, 1, 6, 44));
    }

    @Test
    void testGetMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/getData -- GET", docUri, 13, 1, 13, 24));
    }

    @Test
    void testGetMappingSymbolWithoutPath() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/ -- GET", docUri, 41, 1, 41, 16));
    }

    @Test
    void testGetMappingSymbolWithoutAnything() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/ -- GET", docUri, 45, 1, 45, 14));
    }

    @Test
    void testDeleteMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/deleteData -- DELETE", docUri, 21, 1, 21, 30));
    }

    @Test
    void testPostMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/postData -- POST", docUri, 25, 1, 25, 26));
    }

    @Test
    void testPutMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/putData -- PUT", docUri, 17, 1, 17, 24));
    }

    @Test
    void testPatchMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/patchData -- PATCH", docUri, 29, 1, 29, 28));
    }

    @Test
    void testGetRequestMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/getHello -- GET", docUri, 33, 1, 33, 61));
    }

    @Test
    void testMultiRequestMethodMappingSymbol() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMethodClass.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols =  indexer.getSymbols(docUri);
        assertTrue(containsSymbol(symbols, "@/postAndPutHello -- POST,PUT", docUri, 37, 1, 37, 76));
    }

    @Test
    void testMediaTypes() throws Exception {
        String docUri = directory.toPath().resolve("src/main/java/org/test/RequestMappingMediaTypes.java").toUri().toString();
        List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
        assertEquals(7, symbols.size());
        assertTrue(containsSymbol(symbols, "@/consume1 -- HEAD - Accept: testconsume", docUri, 8, 1, 8, 90));
        assertTrue(containsSymbol(symbols, "@/consume2 - Accept: text/plain", docUri, 13, 1, 13, 73));
        assertTrue(containsSymbol(symbols, "@/consume3 - Accept: text/plain,testconsumetype", docUri, 18, 1, 18, 94));
        assertTrue(containsSymbol(symbols, "@/produce1 - Content-Type: testproduce", docUri, 23, 1, 23, 60));
        assertTrue(containsSymbol(symbols, "@/produce2 - Content-Type: text/plain", docUri, 28, 1, 28, 73));
        assertTrue(containsSymbol(symbols, "@/produce3 - Content-Type: text/plain,testproducetype", docUri, 33, 1, 33, 94));
        assertTrue(containsSymbol(symbols, "@/everything - Accept: application/json,text/plain,testconsume - Content-Type: application/json", docUri, 38, 1, 38, 170));
    }

	private boolean containsSymbol(List<? extends WorkspaceSymbol> symbols, String name, String uri, int startLine, int startCHaracter, int endLine, int endCharacter) {
		for (Iterator<? extends WorkspaceSymbol> iterator = symbols.iterator(); iterator.hasNext();) {
			WorkspaceSymbol symbol = iterator.next();

			if (symbol.getName().equals(name)
					&& symbol.getLocation().getLeft().getUri().equals(uri)
					&& symbol.getLocation().getLeft().getRange().getStart().getLine() == startLine
					&& symbol.getLocation().getLeft().getRange().getStart().getCharacter() == startCHaracter
					&& symbol.getLocation().getLeft().getRange().getEnd().getLine() == endLine
					&& symbol.getLocation().getLeft().getRange().getEnd().getCharacter() == endCharacter) {
				return true;
			}
 		}

		return false;
	}
	
	private void assertSymbol(String docUri, String name, String coveredText) throws Exception {
		List<? extends WorkspaceSymbol> symbols = indexer.getSymbols(docUri);
		Optional<? extends WorkspaceSymbol> maybeSymbol = symbols.stream().filter(s -> name.equals(s.getName())).findFirst();
		assertTrue(maybeSymbol.isPresent());
		
		TextDocument doc = new TextDocument(docUri, LanguageId.JAVA);
		doc.setText(FileUtils.readFileToString(UriUtil.toFile(docUri), Charset.defaultCharset()));
		
		WorkspaceSymbol symbol = maybeSymbol.get();
		Location loc = symbol.getLocation().getLeft();
		assertEquals(docUri, loc.getUri());
		int start = doc.toOffset(loc.getRange().getStart());
		int end = doc.toOffset(loc.getRange().getEnd());
		String actualCoveredText = doc.textBetween(start, end);
		assertEquals(coveredText, actualCoveredText);
	}
}
