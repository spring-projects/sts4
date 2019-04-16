/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxHandlerInformation;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Assert;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(SymbolProviderTestConf.class)
public class WebFluxMappingSymbolProviderTest {

	@Autowired
	private BootLanguageServerHarness harness;

	@Autowired
	private SpringSymbolIndex indexer;

	@Autowired
	JavaProjectFinder projectFinder;

	private File directory;

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());
		String projectDir = directory.toURI().toString();

		// trigger project creation
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();

		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	@Test
	public void testSimpleRequestMappingSymbol() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/UserController.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(4, symbols.size());
		assertTrue(containsSymbol(symbols, "@/users - Content-Type: application/json", docUri, 13, 1, 13, 74));
		assertTrue(containsSymbol(symbols, "@/users/{username} - Content-Type: application/json", docUri, 18, 1, 18, 85));

		List<? extends SymbolAddOnInformation> addon = indexer.getAdditonalInformation(docUri);
		assertEquals(1, addon.size());
		assertEquals("userController", ((BeansSymbolAddOnInformation)addon.get(0)).getBeanID());
	}

	@Test
	public void testRoutesMappingSymbols() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/QuoteRouter.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(6, symbols.size());
		assertTrue(containsSymbol(symbols, "@/hello -- GET - Accept: text/plain", docUri, 22, 5, 22, 70));
		assertTrue(containsSymbol(symbols, "@/echo -- POST - Accept: text/plain - Content-Type: text/plain", docUri, 23, 5, 23, 101));
		assertTrue(containsSymbol(symbols, "@/quotes -- GET - Accept: application/json", docUri, 24, 5, 24, 86));
		assertTrue(containsSymbol(symbols, "@/quotes -- GET - Accept: application/stream+json", docUri, 25, 5, 25, 94));

		List<? extends SymbolAddOnInformation> addons = indexer.getAdditonalInformation(docUri);
		assertEquals(10, addons.size());

		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "/hello", "GET").get(0);
		assertEquals("/hello", handlerInfo1.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo1.getHttpMethods()));
		assertEquals(0, handlerInfo1.getContentTypes().length);
		assertEquals("[TEXT_PLAIN]", Arrays.toString(handlerInfo1.getAcceptTypes()));
		assertEquals("org.test.QuoteHandler", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> hello(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "/echo", "POST").get(0);
		assertEquals("/echo", handlerInfo2.getPath());
		assertEquals("[POST]", Arrays.toString(handlerInfo2.getHttpMethods()));
		assertEquals("[TEXT_PLAIN]", Arrays.toString(handlerInfo2.getContentTypes()));
		assertEquals("[TEXT_PLAIN]", Arrays.toString(handlerInfo2.getAcceptTypes()));
		assertEquals("org.test.QuoteHandler", handlerInfo2.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> echo(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "/quotes", "GET").get(0);
		assertEquals("/quotes", handlerInfo3.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo3.getHttpMethods()));
		assertEquals(0, handlerInfo3.getContentTypes().length);
		assertEquals("[APPLICATION_STREAM_JSON]", Arrays.toString(handlerInfo3.getAcceptTypes()));
		assertEquals("org.test.QuoteHandler", handlerInfo3.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> streamQuotes(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo4 = getWebfluxHandler(addons, "/quotes", "GET").get(1);
		assertEquals("/quotes", handlerInfo4.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo4.getHttpMethods()));
		assertEquals(0, handlerInfo4.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo4.getAcceptTypes()));
		assertEquals("org.test.QuoteHandler", handlerInfo4.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> fetchQuotes(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo4.getHandlerMethod());
	}

	@Test
	public void testNestedRoutesMappingSymbols1() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/NestedRouter1.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(5, symbols.size());
		assertTrue(containsSymbol(symbols, "@/person/{id} -- GET - Accept: application/json", docUri, 27, 6, 27, 45));
		assertTrue(containsSymbol(symbols, "@/person/ -- POST - Content-Type: application/json", docUri, 29, 6, 29, 83));
		assertTrue(containsSymbol(symbols, "@/person -- GET - Accept: application/json", docUri, 28, 7, 28, 60));

		List<? extends SymbolAddOnInformation> addons = indexer.getAdditonalInformation(docUri);
		assertEquals(8, addons.size());

		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "/person/{id}", "GET").get(0);
		assertEquals("/person/{id}", handlerInfo1.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo1.getHttpMethods()));
		assertEquals(0, handlerInfo1.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo1.getAcceptTypes()));
		assertEquals("org.test.PersonHandler1", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "/person/", "POST").get(0);
		assertEquals("/person/", handlerInfo2.getPath());
		assertEquals("[POST]", Arrays.toString(handlerInfo2.getHttpMethods()));
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo2.getContentTypes()));
		assertEquals(0, handlerInfo2.getAcceptTypes().length);
		assertEquals("org.test.PersonHandler1", handlerInfo2.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> createPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "/person", "GET").get(0);
		assertEquals("/person", handlerInfo3.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo3.getHttpMethods()));
		assertEquals(0, handlerInfo3.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo3.getAcceptTypes()));
		assertEquals("org.test.PersonHandler1", handlerInfo3.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> listPeople(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());
	}

	@Test
	public void testNestedRoutesMappingSymbols2() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/NestedRouter2.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(5, symbols.size());
		assertTrue(containsSymbol(symbols, "@/person/{id} -- GET - Accept: application/json", docUri, 29, 6, 29, 45));
		assertTrue(containsSymbol(symbols, "@/ -- POST - Accept: application/json - Content-Type: application/json,application/pdf", docUri, 31, 6, 31, 117));
		assertTrue(containsSymbol(symbols, "@/person -- GET,HEAD - Accept: text/plain,application/json", docUri, 30, 7, 30, 113));

		List<? extends SymbolAddOnInformation> addons = indexer.getAdditonalInformation(docUri);
		assertEquals(8, addons.size());

		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "/person/{id}", "GET").get(0);
		assertEquals("/person/{id}", handlerInfo1.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo1.getHttpMethods()));
		assertEquals(0, handlerInfo1.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo1.getAcceptTypes()));
		assertEquals("org.test.PersonHandler2", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "/", "POST").get(0);
		assertEquals("/", handlerInfo2.getPath());
		assertEquals("[POST]", Arrays.toString(handlerInfo2.getHttpMethods()));
		assertEquals("[APPLICATION_JSON, APPLICATION_PDF]", Arrays.toString(handlerInfo2.getContentTypes()));
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo2.getAcceptTypes()));
		assertEquals("org.test.PersonHandler2", handlerInfo2.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> createPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "/person", "HEAD").get(0);
		assertEquals("/person", handlerInfo3.getPath());
		assertEquals("[GET, HEAD]", Arrays.toString(handlerInfo3.getHttpMethods()));
		assertEquals(0, handlerInfo3.getContentTypes().length);
		assertEquals("[TEXT_PLAIN, APPLICATION_JSON]", Arrays.toString(handlerInfo3.getAcceptTypes()));
		assertEquals("org.test.PersonHandler2", handlerInfo3.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> listPeople(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());
	}

	@Test
	public void testNestedRoutesMappingSymbols3() throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/NestedRouter3.java").toUri().toString();
		List<? extends SymbolInformation> symbols = indexer.getSymbols(docUri);
		assertEquals(8, symbols.size());

		assertTrue(containsSymbol(symbols, "@/person/sub1/sub2/{id} -- GET - Accept: application/json", docUri, 29, 7, 29, 46));
		assertTrue(containsSymbol(symbols, "@/person/sub1/sub2 -- GET - Accept: application/json", docUri, 30, 8, 30, 61));
		assertTrue(containsSymbol(symbols, "@/person/sub1/sub2/nestedGet -- GET", docUri, 31, 9, 31, 56));
		assertTrue(containsSymbol(symbols, "@/person/sub1/andNestPath/andNestPathGET -- GET", docUri, 33, 5, 33, 54));
		assertTrue(containsSymbol(symbols, "@/person/ -- POST - Content-Type: application/json", docUri, 34, 5, 34, 82));
		assertTrue(containsSymbol(symbols, "@/nestedDelete -- DELETE", docUri, 35, 42, 35, 93));

		List<? extends SymbolAddOnInformation> addons = indexer.getAdditonalInformation(docUri);
		assertEquals(14, addons.size());

		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "/person/sub1/sub2/{id}", "GET").get(0);
		assertEquals("/person/sub1/sub2/{id}", handlerInfo1.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo1.getHttpMethods()));
		assertEquals(0, handlerInfo1.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo1.getAcceptTypes()));
		assertEquals("org.test.PersonHandler3", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "/person/sub1/sub2", "GET").get(0);
		assertEquals("/person/sub1/sub2", handlerInfo2.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo2.getHttpMethods()));
		assertEquals(0, handlerInfo2.getContentTypes().length);
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo2.getAcceptTypes()));
		assertEquals("org.test.PersonHandler3", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> listPeople(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "/person/sub1/sub2/nestedGet", "GET").get(0);
		assertEquals("/person/sub1/sub2/nestedGet", handlerInfo3.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo3.getHttpMethods()));
		assertEquals(0, handlerInfo3.getContentTypes().length);
		assertEquals(0, handlerInfo3.getAcceptTypes().length);
		assertEquals("org.test.PersonHandler3", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo4 = getWebfluxHandler(addons, "/person/sub1/andNestPath/andNestPathGET", "GET").get(0);
		assertEquals("/person/sub1/andNestPath/andNestPathGET", handlerInfo4.getPath());
		assertEquals("[GET]", Arrays.toString(handlerInfo4.getHttpMethods()));
		assertEquals(0, handlerInfo4.getContentTypes().length);
		assertEquals(0, handlerInfo4.getAcceptTypes().length);
		assertEquals("org.test.PersonHandler3", handlerInfo4.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo4.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo5 = getWebfluxHandler(addons, "/person/", "POST").get(0);
		assertEquals("/person/", handlerInfo5.getPath());
		assertEquals("[POST]", Arrays.toString(handlerInfo5.getHttpMethods()));
		assertEquals("[APPLICATION_JSON]", Arrays.toString(handlerInfo5.getContentTypes()));
		assertEquals(0, handlerInfo5.getAcceptTypes().length);
		assertEquals("org.test.PersonHandler3", handlerInfo5.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> createPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo5.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo6 = getWebfluxHandler(addons, "/nestedDelete", "DELETE").get(0);
		assertEquals("/nestedDelete", handlerInfo6.getPath());
		assertEquals("[DELETE]", Arrays.toString(handlerInfo6.getHttpMethods()));
		assertEquals(0, handlerInfo6.getContentTypes().length);
		assertEquals(0, handlerInfo6.getAcceptTypes().length);
		assertEquals("org.test.PersonHandler3", handlerInfo6.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> deletePerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo6.getHandlerMethod());
	}

	private boolean containsSymbol(List<? extends SymbolInformation> symbols, String name, String uri, int startLine, int startCHaracter, int endLine, int endCharacter) {
		for (Iterator<? extends SymbolInformation> iterator = symbols.iterator(); iterator.hasNext();) {
			SymbolInformation symbol = iterator.next();

			if (symbol.getName().equals(name)
					&& symbol.getLocation().getUri().equals(uri)
					&& symbol.getLocation().getRange().getStart().getLine() == startLine
					&& symbol.getLocation().getRange().getStart().getCharacter() == startCHaracter
					&& symbol.getLocation().getRange().getEnd().getLine() == endLine
					&& symbol.getLocation().getRange().getEnd().getCharacter() == endCharacter) {
				return true;
			}
 		}

		return false;
	}

	private List<WebfluxHandlerInformation> getWebfluxHandler(List<? extends SymbolAddOnInformation> addons, String path, String httpMethod) {
		return addons.stream()
				.filter((obj) -> obj instanceof WebfluxHandlerInformation)
				.map((obj -> (WebfluxHandlerInformation) obj))
				.filter((addon) -> addon.getPath().equals(path) && Arrays.asList(addon.getHttpMethods()).contains(httpMethod))
				.collect(Collectors.toList());
	}

}
