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
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

public class RequestMappingLiveHoverTest {

	private LanguageServerHarness<BootJavaLanguageServer> harness;
	private MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {

		mockAppProvider = new MockRunningAppProvider();
		harness = BootLanguageServerHarness.builder()
				.runningAppProvider(mockAppProvider.provider)
				.build();
	}

	@Test
	public void testLiveHoverHintTypeMapping() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/HelloWorldController.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.build();

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

	@Test
	public void testDeleteMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[DELETE]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.deleteGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.DeleteMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
            		"@DeleteMapping(\"/greetings\")\n" +
            		"public void deleteGreetings() {\n" +
            		"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@DeleteMapping(\"/greetings\")", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}


	@Test
	public void testDeleteMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.deleteGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.DeleteMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
            		"@DeleteMapping(\"/greetings\")\n" +
            		"public void deleteGreetings() {\n" +
            		"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@DeleteMapping(\"/greetings\")");

	}

	@Test
	public void testGetMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.greetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                	"import java.lang.String;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
            		"@GetMapping(\"/greetings\")\n" +
            		"public String greetings() {\n" +
            		"return \"Greetings!\";\n" +
            		"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@GetMapping(\"/greetings\")", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testGetMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[DELETE]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.greetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import java.lang.String;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.GetMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
            		"@GetMapping(\"/greetings\")\n" +
            		"public String greetings() {\n" +
            		"return \"Greetings!\";\n" +
            		"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@GetMapping(\"/greetings\")");

	}

	@Test
	public void testPostMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[POST]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.createGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PostMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PostMapping(\"/greetings\")\n" +
                "public void createGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@PostMapping(\"/greetings\")", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testPostMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.createGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PostMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PostMapping(\"/greetings\")\n" +
                "public void createGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@PostMapping(\"/greetings\")");

	}

	@Test
	public void testPutMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[PUT]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.updateGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PutMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PutMapping(\"/greetings\")\n" +
                "public void updateGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@PutMapping(\"/greetings\")", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testPutMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.updateGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PutMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PutMapping(\"/greetings\")\n" +
                "public void updateGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@PutMapping(\"/greetings\")");

	}

	@Test
	public void testPatchMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[PATCH]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.patchGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PatchMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PatchMapping(\"/greetings\")\n" +
                "public void patchGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@PatchMapping(\"/greetings\")", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testPatchMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.patchGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.PatchMapping;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@PatchMapping(\"/greetings\")\n" +
                "public void patchGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@PatchMapping(\"/greetings\")");

	}

	@Test
	public void testMultiRequestMethodMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[POST,PUT]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.updateGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value=\"/greetings\", method={POST, PUT})\n" +
                "public void updateGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@RequestMapping(value=\"/greetings\", method={POST, PUT})", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testMultiRequestMethodMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings],methods=[POST,PUT,PATCH]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.updateGreetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value=\"/greetings\", method={POST, PUT})\n" +
                "public void updateGreetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@RequestMapping(value=\"/greetings\", method={POST, PUT})");

	}

	@Test
	public void testMultiPathMappingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings || /hello],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.greetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)\n" +
                "public String greetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)", "Path: [/greetings](http://cfapps.io:999/greetings)\n" +
				"Path: [/hello](http://cfapps.io:999/hello)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testMultiPathMappingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/greetings || /hello || /helloAgain],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.greetings()\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)\n" +
                "public String greetings() {\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertNoHover("@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)");

	}

	@Test
	public void testMethodMatchingHoverHintMethod1() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public org.springframework.http.ResponseEntity<?> com.example.RestApi.find(java.lang.String,java.util.Date,java.lang.String)\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                	"import org.springframework.http.ResponseEntity;\n" +
                "import java.lang.String;\n" +
                	"import java.util.Date;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value=\"/find\", method=GET)\n" +
                "public ResponseEntity<?> find(String p1, Date p2, String p3) {\n" +
                "return null;\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "Path: [/find](http://cfapps.io:999/find)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testMethodMatchingHoverHintMethod2() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String,java.util.Map<java.lang.String, java.lang.String>)\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                	"import org.springframework.http.ResponseEntity;\n" +
                "import java.lang.String;\n" +
                	"import java.util.Map;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value=\"/find\", method=GET)\n" +
                "public Object set(String p1, Map<String, String> p2) {\n" +
                "return null;\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "Path: [/find](http://cfapps.io:999/find)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}

	@Test
	public void testMethodMatchingHoverHintMethod3() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = "file://" +directory.getAbsolutePath() + "/src/main/java/example/RestApi.java";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.getRequestMappings(
				"{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String,java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>)\"}}")
		.	build();

		harness.intialize(directory);

		Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                "\n" +
                	"import org.springframework.http.ResponseEntity;\n" +
                "import java.lang.String;\n" +
                "import java.lang.Integer;\n" +
                	"import java.util.Map;\n" +
                "import org.springframework.stereotype.Controller;\n" +
                "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                "\n" +
                "@Controller\n" +
                "public class RestApi {\n" +
                "\n" +
                "@RequestMapping(value=\"/find\", method=GET)\n" +
                "public Object set(String p1, Map<String, Map<String, Integer>> p2) {\n" +
                "return null;\n" +
        			"}\n" +
				"\n" +
                "}",
        docUri);

		editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "Path: [/find](http://cfapps.io:999/find)\n" +
				"\n" +
				"Process ID: 76543\n" +
				"\n" +
				"Process Name: test-request-mapping-live-hover");

	}


}
