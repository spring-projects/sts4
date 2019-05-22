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
	public void testBoot1xActualActuatorEnvProp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "1.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_1x_ENV)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromEnv/hello-world](https://cfapps.io:1111/fromEnv/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot1xActualActuatorCommandArgCamel() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "1.x";

		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_1x_COMMAND_LINE_ARG_CAMEL_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromlaunchconfig/hello-world](https://cfapps.io:1111/fromlaunchconfig/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot1xActualActuatorCommandArgKebab() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "1.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_1x_COMMAND_LINE_ARG_KEBAB_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromlaunchconfig/hello-world](https://cfapps.io:1111/fromlaunchconfig/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot1xActualActuatorAppConfigFileKebab() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "1.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_1x_APP_CONFIG_FILE_KEBAB_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/frompropsfile/hello-world](https://cfapps.io:1111/frompropsfile/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot1xActualActuatorAppConfigFileCamel() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "1.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_1x_APP_CONFIG_FILE_CAMEL_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/frompropsfile/hello-world](https://cfapps.io:1111/frompropsfile/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorEnvProp() throws Exception {

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_ENV)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromenvironment/hello-world](https://cfapps.io:1111/fromenvironment/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorCommandArgCamel() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";

		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_COMMAND_LINE_ARG_CAMEL_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromlaunchconfig/hello-world](https://cfapps.io:1111/fromlaunchconfig/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorCommandArgKebab() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_COMMAND_LINE_ARG_KEBAB_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromlaunchconfig/hello-world](https://cfapps.io:1111/fromlaunchconfig/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorAppConfigFileKebab() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_APP_CONFIG_FILE_KEBAB_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/frompropsfile/hello-world](https://cfapps.io:1111/frompropsfile/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorAppConfigFileCamel() throws Exception {


		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_APP_CONFIG_FILE_CAMEL_CASE)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/frompropsfile/hello-world](https://cfapps.io:1111/frompropsfile/hello-world)  \n" +
				"Process [PID=22022, name=`test-request-mapping-live-hover`]");

	}

	@Test
	public void testBoot2xActualActuatorPropertySourcePriority() throws Exception {

		//  Test that for Boot 2.x, if context path property appears in three different sources:
		//   env var, command line arg, and app config file, that the highest priority source is read, in this case command line arg

		File directory = new File(
				ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
		String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
				.toString();

		String bootVersion = "2.x";


		// Build a mock running boot app
		mockAppProvider.builder()
			.isSpringBootApp(true)
			.port("1111")
			.processId("22022")
			.host("cfapps.io")
			.urlScheme("https")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.contextPathEnvJson(bootVersion, AcuatorEnvTestConstants.BOOT_2x_PROPERTY_SOURCE_PRIORITY)
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");

		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/fromlaunchconfig/hello-world](https://cfapps.io:1111/fromlaunchconfig/hello-world)  \n" +
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
			.urlScheme("https")
			.contextPath("/mockedpath")
			.processName("test-request-mapping-live-hover")
			// Ugly, but this is real JSON copied from a real live running app. We want the
			// mock app to return realistic results if possible
			.requestMappingsJson(
				"{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
			.build();

		harness.intialize(directory);

		Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
		editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");
		editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/mockedpath/hello-world](https://cfapps.io:1111/mockedpath/hello-world)  \n" +
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
			.urlScheme("https")
			.contextPath("/mockedpath")
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

		editor.assertHoverContains("@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)", "[https://cfapps.io:999/mockedpath/greetings](https://cfapps.io:999/mockedpath/greetings)  \n" +
				"[https://cfapps.io:999/mockedpath/hello](https://cfapps.io:999/mockedpath/hello)  \n" +
				"Process [PID=76543, name=`test-request-mapping-live-hover`]");

	}


}
