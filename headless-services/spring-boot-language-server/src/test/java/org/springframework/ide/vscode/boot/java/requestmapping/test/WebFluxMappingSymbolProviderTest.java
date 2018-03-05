/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.SymbolInformation;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.requestmapping.WebfluxHandlerInformation;
import org.springframework.ide.vscode.project.harness.BootJavaLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * @author Martin Lippert
 */
public class WebFluxMappingSymbolProviderTest {

	private BootJavaLanguageServerHarness harness;

	@Before
	public void setup() throws Exception {
		harness = BootJavaLanguageServerHarness.builder().build();
	}

	@Test
	public void testSimpleRequestMappingSymbol() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/UserController.java").toUri().toString();
		List<? extends SymbolInformation> symbols = getSymbols(docUri);
		assertEquals(4, symbols.size());
		assertTrue(containsSymbol(symbols, "@/users", docUri, 19, 1, 19, 74));
		assertTrue(containsSymbol(symbols, "@/users/{username}", docUri, 24, 1, 24, 85));
		
		List<? extends Object> addons = getAdditionalInformation(docUri);
		assertNull(addons);
	}

	@Test
	public void testRoutesMappingSymbols() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/QuoteRouter.java").toUri().toString();
		List<? extends SymbolInformation> symbols = getSymbols(docUri);
		assertEquals(6, symbols.size());
		assertTrue(containsSymbol(symbols, "@/hello -- GET", docUri, 22, 5, 22, 70));
		assertTrue(containsSymbol(symbols, "@/echo -- POST", docUri, 23, 5, 23, 101));
		assertTrue(containsSymbol(symbols, "@/quotes -- GET", docUri, 24, 5, 24, 86));
		assertTrue(containsSymbol(symbols, "@/quotes -- GET", docUri, 25, 5, 25, 94));
		
		List<? extends Object> addons = getAdditionalInformation(docUri);
		assertEquals(4, addons.size());
		
		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "@/hello -- GET").get(0);
		assertEquals("@/hello -- GET", handlerInfo1.getSymbol());
		assertEquals("org.test.QuoteHandler", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> hello(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());
		
		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "@/echo -- POST").get(0);
		assertEquals("@/echo -- POST", handlerInfo2.getSymbol());
		assertEquals("org.test.QuoteHandler", handlerInfo2.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> echo(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "@/quotes -- GET").get(0);
		assertEquals("@/quotes -- GET", handlerInfo3.getSymbol());
		assertEquals("org.test.QuoteHandler", handlerInfo3.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> streamQuotes(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo4 = getWebfluxHandler(addons, "@/quotes -- GET").get(1);
		assertEquals("@/quotes -- GET", handlerInfo4.getSymbol());
		assertEquals("org.test.QuoteHandler", handlerInfo4.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> fetchQuotes(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo4.getHandlerMethod());
	}

	@Test
	public void testNestedRoutesMappingSymbols() throws Exception {
		harness.intialize(new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI()));
		File directory = new File(ProjectsHarness.class.getResource("/test-projects/test-webflux-project/").toURI());

		String docUri = directory.toPath().resolve("src/main/java/org/test/NestedRouter.java").toUri().toString();
		List<? extends SymbolInformation> symbols = getSymbols(docUri);
		assertEquals(5, symbols.size());
		assertTrue(containsSymbol(symbols, "@/person/{id} -- GET", docUri, 27, 6, 27, 45));
		assertTrue(containsSymbol(symbols, "@/person/ -- POST", docUri, 29, 6, 29, 83));
		assertTrue(containsSymbol(symbols, "@/person -- GET", docUri, 28, 7, 28, 60));

		List<? extends Object> addons = getAdditionalInformation(docUri);
		assertEquals(3, addons.size());
		
		WebfluxHandlerInformation handlerInfo1 = getWebfluxHandler(addons, "@/person/{id} -- GET").get(0);
		assertEquals("@/person/{id} -- GET", handlerInfo1.getSymbol());
		assertEquals("org.test.PersonHandler", handlerInfo1.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> getPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo1.getHandlerMethod());
		
		WebfluxHandlerInformation handlerInfo2 = getWebfluxHandler(addons, "@/person/ -- POST").get(0);
		assertEquals("@/person/ -- POST", handlerInfo2.getSymbol());
		assertEquals("org.test.PersonHandler", handlerInfo2.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> createPerson(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo2.getHandlerMethod());

		WebfluxHandlerInformation handlerInfo3 = getWebfluxHandler(addons, "@/person -- GET").get(0);
		assertEquals("@/person -- GET", handlerInfo3.getSymbol());
		assertEquals("org.test.PersonHandler", handlerInfo3.getHandlerClass());
		assertEquals("public Mono<org.springframework.web.reactive.function.server.ServerResponse> listPeople(org.springframework.web.reactive.function.server.ServerRequest)", handlerInfo3.getHandlerMethod());
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

	private List<? extends SymbolInformation> getSymbols(String docUri) {
		return harness.getServerWrapper().getComponents().getSpringIndexer().getSymbols(docUri);
	}

	private List<? extends Object> getAdditionalInformation(String docUri) {
		return harness.getServerWrapper().getComponents().getSpringIndexer().getAdditonalInformation(docUri);
	}
	
	private List<WebfluxHandlerInformation> getWebfluxHandler(List<? extends Object> addons, String symbol) {
		return addons.stream()
				.filter((obj) -> obj instanceof WebfluxHandlerInformation)
				.map((obj -> (WebfluxHandlerInformation) obj))
				.filter((addon) -> addon.getSymbol().equals(symbol))
				.collect(Collectors.toList());
	}

}
