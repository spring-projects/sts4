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

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRunningAppProvider;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class RequestMappingLiveHoverTestWithContextPath {

	@Autowired BootLanguageServerHarness harness;
	@Autowired MockRunningAppProvider mockAppProvider;

	@Before
	public void setup() throws Exception {
		harness.useProject(ProjectsHarness.INSTANCE.mavenProject("test-request-mapping-live-hover"));
	}

	@Test
	public void testActuatorEnvOrderedPropertySourceCamelCase() throws Exception {
		// Tests an actuator env json that contains camel case context path in command line arg and application config.

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson("2.x", AcuatorEnvTestConstants.BOOT_2x_ENV_CONTEX_PATH_CAMEL_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		// test that the command line arg context path appears
		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[http://cfapps.io:1111/pathfromcommandlineargs/hello-world](http://cfapps.io:1111/pathfromcommandlineargs/hello-world)  \n" +
				"\n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testActuatorEnvOrderedPropertySourceKebabCase() throws Exception {

		// Tests an actuator env json that contains kebab case context path in command line arg and application config.

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson("2.x", AcuatorEnvTestConstants.BOOT_2x_ENV_CONTEX_PATH_KEBAB_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		// test that the command line arg context path appears
		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[http://cfapps.io:1111/pathfromcommandlineargs/hello-world](http://cfapps.io:1111/pathfromcommandlineargs/hello-world)  \n" +
				"\n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}


	@Test
	public void testWithMockedContextPath() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.contextPath("/adifferentpath")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");
		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[http://cfapps.io:1111/adifferentpath/hello-world](http://cfapps.io:1111/adifferentpath/hello-world)  \n" +
				"\n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testMultiPathMockedContextPath() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
				.toString();


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("999")
			.processId("76543")
			.host("cfapps.io")
			.contextPath("/differentPaath")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
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

		editor.assertHoverContains("@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)", "[http://cfapps.io:999/differentPaath/greetings](http://cfapps.io:999/differentPaath/greetings)  \n" +
				"[http://cfapps.io:999/differentPaath/hello](http://cfapps.io:999/differentPaath/hello)  \n" +
				"\n" +
				"Process [PID=76543, name=`test-request-mapping-live-hover`]");

	}


}
