/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.BootLanguageServerParams;
import org.springframework.ide.vscode.boot.bootiful.AdHocPropertyHarnessTestConf;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.editor.harness.PropertyIndexHarness;
import org.springframework.ide.vscode.boot.java.handlers.BootJavaReconcileEngine;
import org.springframework.ide.vscode.boot.java.links.SourceLinkFactory;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCache;
import org.springframework.ide.vscode.boot.java.utils.SymbolCacheVoid;
import org.springframework.ide.vscode.boot.metadata.ValueProviderRegistry;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.maven.java.MavenJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import({AdHocPropertyHarnessTestConf.class, ValueSpelExpressionValidationTest.TestConf.class})
public class ValueSpelExpressionValidationTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private IJavaProject testProject;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private CompilationUnitCache compilationUnitCache;
	@Autowired private SimpleLanguageServer server;

	private File directory;
	private String docUri;
	private TestProblemCollector problemCollector;
	private BootJavaReconcileEngine reconcileEngine;

	@Configuration
	static class TestConf {

		//Somewhat strange test setup, test provides a specific test project.
		//The project finder finds this test project,
		//But it is not used in the indexProvider/harness.
		//this is a bit odd... but we preserved the strangeness how it was.

		@Bean MavenJavaProject testProject() throws Exception {
			return ProjectsHarness.INSTANCE.mavenProject("test-annotations");
		}

		@Bean PropertyIndexHarness indexHarness(ValueProviderRegistry valueProviders) {
			return new PropertyIndexHarness(valueProviders);
		}

		@Bean JavaProjectFinder projectFinder(MavenJavaProject testProject) {
			return new JavaProjectFinder() {

				@Override
				public Optional<IJavaProject> find(TextDocumentIdentifier doc) {
					return Optional.ofNullable(testProject);
				}

				@Override
				public Collection<? extends IJavaProject> all() {
					// TODO Auto-generated method stub
					return testProject == null ? Collections.emptyList() : ImmutableList.of(testProject);
				}
			};
		}

		@Bean BootLanguageServerHarness harness(SimpleLanguageServer server, BootLanguageServerParams serverParams, PropertyIndexHarness indexHarness, JavaProjectFinder projectFinder) throws Exception {
			return new BootLanguageServerHarness(server, serverParams, indexHarness, projectFinder, LanguageId.JAVA, ".java");
		}

		@Bean BootLanguageServerParams serverParams(SimpleLanguageServer server, JavaProjectFinder projectFinder, ValueProviderRegistry valueProviders, PropertyIndexHarness indexHarness) {
			BootLanguageServerParams testDefaults = BootLanguageServerHarness.createTestDefault(server, valueProviders);
			return new BootLanguageServerParams(
					projectFinder,
					ProjectObserver.NULL,
					indexHarness.getIndexProvider(),
					testDefaults.typeUtilProvider
			);
		}

		@Bean SymbolCache symbolCache() {
			return new SymbolCacheVoid();
		}

		@Bean SourceLinks sourceLinks(SimpleTextDocumentService documents, CompilationUnitCache cuCache) {
			return SourceLinkFactory.NO_SOURCE_LINKS;
		}

	}

	@Before
	public void setup() throws Exception {
		harness.intialize(null);
		
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-annotations/").toURI());
		docUri = directory.toPath().resolve("src/main/java/org/test/TestValueCompletion.java").toUri().toString();

		problemCollector = new TestProblemCollector();
		reconcileEngine = new BootJavaReconcileEngine(compilationUnitCache, projectFinder);
	}
	
	@After
	public void closeDoc() throws Exception {
		TextDocumentIdentifier identifier = new TextDocumentIdentifier(docUri);
		DidCloseTextDocumentParams closeParams = new DidCloseTextDocumentParams(identifier);
		server.getTextDocumentService().didClose(closeParams);
		server.getAsync().waitForAll();
	}
	
	@Test
	public void testNoSpelExpressionFound() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(\"something\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}

	@Test
	public void testCorrectSpelExpressionFound() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(\"#{new String('hello world').toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}

	@Test
	public void testCorrectSpelExpressionFoundWithParamName() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(value=\"#{new String('hello world').toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}

	@Test
	public void testIncorrectSpelExpressionFound() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(\"#{new String('hello world).toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundWithParamName() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(value=\"#{new String('hello world).toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundOnMethodParameter() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onParameter\")", "@Value(\"#{new String('hello world).toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundOnMethodParameterWithParamName() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onParameter\")", "@Value(value=\"#{new String('hello world).toUpperCase()}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundOnSpelParamOfCachableAnnotation() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Cacheable(condition=\"new String('hello world).toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionNotFoundOnNonSpelParamOfCachableAnnotation() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Cacheable(keyGenerator=\"new String('hello world).toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundOnSpelParamOfCachableAnnotationAmongOtherParams() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Cacheable(keyGenerator=\"somekey\", condition=\"new String('hello world).toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIncorrectSpelExpressionFoundOnMultipleSpelParamsOfCachableAnnotation() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Cacheable(unless=\"new String('hello world).toUpperCase()\", condition=\"new String('hello world).toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(2, problems.size());
	}
	
	@Test
	public void testCorrectSpelExpressionFoundOnCustomAnnotation() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onMethod\")", "@CustomEventListener(condition=\"new String('hello world').toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}

	@Test
	public void testIncorrectSpelExpressionFoundOnCustomAnnotation() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onMethod\")", "@CustomEventListener(condition=\"new String('hello world).toUpperCase()\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(1, problems.size());
	}
	
	@Test
	public void testIgnoreSpelExpressionsWithPropertyPlaceholder() throws Exception {
		TextDocument doc = prepareDocument("@Value(\"onField\")", "@Value(value=\"#{${property.hello:false}}\")");
		assertNotNull(doc);
		
		reconcileEngine.reconcile(doc, problemCollector);
		
		List<ReconcileProblem> problems = problemCollector.getCollectedProblems();
		assertEquals(0, problems.size());
	}
	
	private TextDocument prepareDocument(String selectedAnnotation, String annotationStatementBeforeTest) throws Exception {
		String content = IOUtils.toString(new URI(docUri));

		TextDocumentItem docItem = new TextDocumentItem(docUri, LanguageId.JAVA.toString(), 0, content);
		DidOpenTextDocumentParams openParams = new DidOpenTextDocumentParams(docItem);
		server.getTextDocumentService().didOpen(openParams);
		server.getAsync().waitForAll();
		
		TextDocument doc = server.getTextDocumentService().get(docUri);
		
		int position = content.indexOf(selectedAnnotation);
		doc.replace(position, selectedAnnotation.length(), annotationStatementBeforeTest);
		
		return doc;
	}
	
	public static class TestProblemCollector implements IProblemCollector {
		
		private List<ReconcileProblem> problems = new ArrayList<>();

		@Override
		public void beginCollecting() {
		}

		@Override
		public void endCollecting() {
		}

		@Override
		public void accept(ReconcileProblem problem) {
			problems.add(problem);
		}
		
		protected List<ReconcileProblem> getCollectedProblems() {
			return problems;
		}
		
	}

}
