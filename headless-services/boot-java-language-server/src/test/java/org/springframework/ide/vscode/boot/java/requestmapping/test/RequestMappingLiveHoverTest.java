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
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.java.CompositeJavaProjectFinder;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.PropertyIndexHarness;

public class RequestMappingLiveHoverTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {

		mockAppProvider = new MockRunningAppProvider();
		harness = BootLanguageServerHarness.createMocked(mockAppProvider.provider);
	}

	@Test
	public void testLiveHoverHintTypeMapping() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/HelloWorldController.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.containsLanguageServerProcessPropery(false)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@RequestMapping(\"/hello-world\")", "Path: [/hello-world](http://cfapps.io:1111/hello-world)\n" +
				"\n" +
				"Process ID: 22022\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testLiveHoverHintMethod() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.containsLanguageServerProcessPropery(false)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHoverContains("@RequestMapping(\"/hello\")", "Path: [/hello](http://cfapps.io:999/hello)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

		editor.assertHoverContains("@RequestMapping(\"/goodbye\")", "Path: [/goodbye](http://cfapps.io:999/goodbye)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testNoLiveHoverNoRunningApp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";

		harness.intialize(directory);

		assertTrue("Expected no mock running boot apps, but found: " + mockAppProvider.mockedApps,
				mockAppProvider.mockedApps.isEmpty());

		Editor editorWithMethodLiveHover = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editorWithMethodLiveHover.assertNoHover("@RequestMapping(\"/hello\")");

		editorWithMethodLiveHover.assertNoHover("@RequestMapping(\"/goodbye\")");

		docUri = "file://" + directory.getAbsolutePath() + "/src/main/java/example/HelloWorldController.java";

		Editor editorWithTypeLiveHover = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editorWithTypeLiveHover.assertNoHover("@RequestMapping(\"/hello-world\")");

	}

}
