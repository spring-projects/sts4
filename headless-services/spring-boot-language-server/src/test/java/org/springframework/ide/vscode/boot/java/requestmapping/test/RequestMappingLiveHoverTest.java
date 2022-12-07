/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
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
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.boot.java.livehover.v2.LiveMetricsModel;
import org.springframework.ide.vscode.boot.java.livehover.v2.RequestMappingMetrics;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveData;
import org.springframework.ide.vscode.boot.java.livehover.v2.SpringProcessLiveDataProvider;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.MockRequestMapping;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.ide.vscode.project.harness.SpringProcessLiveDataBuilder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class RequestMappingLiveHoverTest {

	@Autowired BootLanguageServerHarness harness;
	@Autowired SpringProcessLiveDataProvider liveDataProvider;

	@BeforeEach
	public void setup() throws Exception {
		harness.useProject(ProjectsHarness.INSTANCE.mavenProject("test-request-mapping-live-hover"));
	}
	
	@AfterEach
	public void tearDown() throws Exception {
		liveDataProvider.remove("processkey");
		liveDataProvider.remove("processkey1");
		liveDataProvider.remove("processkey2");
		liveDataProvider.remove("processkey3");
	}

    @Test
    void testLiveHoverHintTypeMapping() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("1111")
                .processID("22022")
                .host("cfapps.io")
                .urlScheme("https")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");
        editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/hello-world](https://cfapps.io:1111/hello-world)  \n" +
                "Process [PID=22022, name=`test-request-mapping-live-hover`]");

    }


    @Test
    void testLiveHoverHintNoPaths() throws Exception {
        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
                .toString();

        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("1111")
                .processID("22022")
                .host("cfapps.io")
                .urlScheme("https")
                .processName("test-request-mapping-live-hover")
                .requestMappings(
                        new MockRequestMapping()
                                .className("example.HelloWorldController")
                                .methodName("sayHello")
                                .methodParams("java.lang.String")
                                .paths()
                )
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(method=RequestMethod.GET)");
        editor.assertHoverContains("@RequestMapping(method=RequestMethod.GET)", "[https://cfapps.io:1111/](https://cfapps.io:1111/)  \n" +
                "Process [PID=22022, name=`test-request-mapping-live-hover`]");


        /* Sample real data:
          {
            "handler":"public java.lang.String com.github.kdvolder.helloworldservice.DemoApplication.hello()",
             "predicate":"{[/hello]}",
             "details":{
                "requestMappingConditions": {
                    "headers":[],
                    "methods":[],
                    "patterns":["/hello"],
                    "produces":[],
                    "params":[],
                    "consumes":[]},
                    "handlerMethod": {
                       "name":"hello",
                       "className":"com.github.kdvolder.helloworldservice.DemoApplication",
                       "descriptor":"()Ljava/lang/String;"
                     }
                 }
              }
         */
    }

    @Test
    void testLiveHoverHintMethod() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(\"/hello\")", "@RequestMapping(\"/goodbye\")");

        editor.assertHoverContains("@RequestMapping(\"/hello\")", "[https://cfapps.io:999/hello](https://cfapps.io:999/hello)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

        editor.assertHoverContains("@RequestMapping(\"/goodbye\")", "[https://cfapps.io:999/goodbye](https://cfapps.io:999/goodbye)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testLiveHoverMetricsSection() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .liveMetrics(new LiveMetricsModel() {

                    @Override
                    public RequestMappingMetrics getRequestMappingMetrics(String[] paths, String[] requestMethods) {
                        return new RequestMappingMetrics() {

                            @Override
                            public double getTotalTime() {
                                return "/hello".equals(paths[0]) ? 3.034567 : 5.43673;
                            }

                            @Override
                            public TimeUnit getTimeUnit() {
                                return TimeUnit.SECONDS;
                            }

                            @Override
                            public double getMaxTime() {
                                return "/hello".equals(paths[0]) ? 0.5535632 : 0.7437624;
                            }

                            @Override
                            public long getCallsCount() {
                                return "/hello".equals(paths[0]) ? 25 : 34;
                            }
                        };
                    }
                })
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(\"/hello\")", "@RequestMapping(\"/goodbye\")");

        editor.assertHoverContains("@RequestMapping(\"/hello\")", "[https://cfapps.io:999/hello](https://cfapps.io:999/hello)  \n");
        editor.assertHoverContains("@RequestMapping(\"/hello\")", "Count: 25 | Total Time: 3.034567s | Max Time: 0.5535632s");
        editor.assertHoverContains("@RequestMapping(\"/hello\")", "Process [PID=76543, name=`test-request-mapping-live-hover`]");

        editor.assertHoverContains("@RequestMapping(\"/goodbye\")", "Count: 34 | Total Time: 5.43673s | Max Time: 0.7437624s");

    }

    @Test
    void testLiveCodeLensMetricsSection() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .liveMetrics(new LiveMetricsModel() {

                    @Override
                    public RequestMappingMetrics getRequestMappingMetrics(String[] paths, String[] requestMethods) {
                        return new RequestMappingMetrics() {

                            @Override
                            public double getTotalTime() {
                                return "/hello".equals(paths[0]) ? 3.034567 : 5.43673;
                            }

                            @Override
                            public TimeUnit getTimeUnit() {
                                return TimeUnit.SECONDS;
                            }

                            @Override
                            public double getMaxTime() {
                                return "/hello".equals(paths[0]) ? 0.5535632 : 0.7437624;
                            }

                            @Override
                            public long getCallsCount() {
                                return "/hello".equals(paths[0]) ? 25 : 34;
                            }
                        };
                    }
                })
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(\"/hello\")", "@RequestMapping(\"/goodbye\")");

        String total1 = String.format("%.2f", 3.03d);
        String max1 = String.format("%.2f", 0.55d);

        String total2 = String.format("%.2f", 5.44d);
        String max2 = String.format("%.2f", 0.74d);

        editor.assertLiveCodeLensContains("@RequestMapping(\"/hello\")", "Count=25 Total=" + total1 + "s Max=" + max1 + "s");
        editor.assertLiveCodeLensContains("@RequestMapping(\"/goodbye\")", "Count=34 Total=" + total2 + "s Max=" + max2 + "s");

    }

    @Test
    void testNoLiveHoverNoRunningApp() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        harness.intialize(directory);

        Editor editorWithMethodLiveHover = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editorWithMethodLiveHover.assertNoHover("@RequestMapping(\"/hello\")");

        editorWithMethodLiveHover.assertNoHover("@RequestMapping(\"/goodbye\")");

        docUri = directory.toPath().resolve("src/main/java/example/HelloWorldController.java").toUri()
                .toString();

        Editor editorWithTypeLiveHover = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editorWithTypeLiveHover.assertNoHover("@RequestMapping(\"/hello-world\")");

    }

    @Test
    void testSimpleMethodHoverHintMethod1() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/greetings],methods=[DELETE]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.deleteGreetings()\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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

        editor.assertHoverContains("@DeleteMapping(\"/greetings\")", "[https://cfapps.io:999/greetings](https://cfapps.io:999/greetings)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }


    @Test
    void testDeleteMappingHoverHintMethod2() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/greetings],methods=[DELETE]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public void com.example.RestApi.deleteGreetings()\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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
                        "public void deleteGreetings(int n) {\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertNoHover("@DeleteMapping(\"/greetings\")");
    }

    @Test
    void testNoHoverHintMethod() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/greetings],methods=[PUT]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.updateGreetings()\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                        "\n" +
                        "import java.lang.String;\n" +
                        "import org.springframework.stereotype.Controller;\n" +
                        "import org.springframework.web.bind.annotation.PutMapping;\n" +
                        "\n" +
                        "@Controller\n" +
                        "public class RestApi {\n" +
                        "\n" +
                        "@PutMapping(\"/greetings\")\n" +
                        "public String updateGreetings(int n) {\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertNoHover("@PutMapping(\"/greetings\")");
    }

    @Test
    void testMultiPathMappingHoverHintMethod1() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/greetings || /hello],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.String com.example.RestApi.greetings()\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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

        editor.assertHoverContains("@RequestMapping(value={\"/greetings\", \"/hello\"}, method=GET)", "[https://cfapps.io:999/greetings](https://cfapps.io:999/greetings)  \n" +
                "[https://cfapps.io:999/hello](https://cfapps.io:999/hello)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testMethodMatchingHoverHintMethod1() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .host("cfapps.io")
                .urlScheme("https")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public org.springframework.http.ResponseEntity<?> com.example.RestApi.find(java.lang.String,java.util.Date,java.lang.String)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testMethodMatchingHoverHintMethod2() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String,java.util.Map<java.lang.String, java.lang.String>)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testMethodMatchingHoverHintMethod3() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String,java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Integer>>)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testWildCardMethodMatchingHoverHint() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String,java.util.Map<java.lang.String, java.util.Map<java.lang.String, ? extends java.lang.Integer>>)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

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
                        "public Object set(String p1, Map<String, Map<String, ? extends Integer>> p2) {\n" +
                        "return null;\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testArrayMethodMatchingHoverHint() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String[])\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                        "\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import java.lang.String;\n" +
                        "import org.springframework.stereotype.Controller;\n" +
                        "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                        "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                        "\n" +
                        "@Controller\n" +
                        "public class RestApi {\n" +
                        "\n" +
                        "@RequestMapping(value=\"/find\", method=GET)\n" +
                        "public Object set(String[] p) {\n" +
                        "return null;\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");

    }

    @Test
    void testMultiDimensionalArrayMethodMatchingHoverHint() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String[][])\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                        "\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import java.lang.String;\n" +
                        "import org.springframework.stereotype.Controller;\n" +
                        "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                        "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                        "\n" +
                        "@Controller\n" +
                        "public class RestApi {\n" +
                        "\n" +
                        "@RequestMapping(value=\"/find\", method=GET)\n" +
                        "public Object set(String[][] p) {\n" +
                        "return null;\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");
    }

    @Test
    void testVarArgsMethodMatchingHoverHint() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();

        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"{[/find],methods=[GET]}\": {\"bean\": \"requestMappingHandlerMapping\", \"method\":\"public java.lang.Object com.example.RestApi.set(java.lang.String...)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditor(LanguageId.JAVA,
                "package com.example;\n" +
                        "\n" +
                        "import org.springframework.http.ResponseEntity;\n" +
                        "import java.lang.String;\n" +
                        "import org.springframework.stereotype.Controller;\n" +
                        "import org.springframework.web.bind.annotation.RequestMapping;\n" +
                        "import org.springframework.web.bind.annotation.RequestMethod.*;\n" +
                        "\n" +
                        "@Controller\n" +
                        "public class RestApi {\n" +
                        "\n" +
                        "@RequestMapping(value=\"/find\", method=GET)\n" +
                        "public Object set(String... p) {\n" +
                        "return null;\n" +
                        "}\n" +
                        "\n" +
                        "}",
                docUri);

        editor.assertHoverContains("@RequestMapping(value=\"/find\", method=GET)", "[https://cfapps.io:999/find](https://cfapps.io:999/find)  \n" +
                "Process [PID=76543, name=`test-request-mapping-live-hover`]");
    }

    @Test
    void testMultipleAppsLiveHover() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();
        harness.intialize(directory);

        // Build three different instances of the same app running on different ports with different process IDs
        SpringProcessLiveData liveData1 = new SpringProcessLiveDataBuilder()
                .port("1000")
                .processID("70000")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        liveDataProvider.add("processkey1", liveData1);

        SpringProcessLiveData liveData2 = new SpringProcessLiveDataBuilder()
                .port("1001")
                .processID("80000")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        liveDataProvider.add("processkey2", liveData2);

        SpringProcessLiveData liveData3 = new SpringProcessLiveDataBuilder()
                .port("1002")
                .processID("90000")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        liveDataProvider.add("processkey3", liveData3);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHoverContains("@RequestMapping(\"/hello\")", "[https://cfapps.io:1000/hello](https://cfapps.io:1000/hello)  \n" +
                "Process [PID=70000, name=`test-request-mapping-live-hover`]\n" +
                "\n" +
                "[https://cfapps.io:1001/hello](https://cfapps.io:1001/hello)  \n" +
                "Process [PID=80000, name=`test-request-mapping-live-hover`]\n" +
                "\n" +
                "[https://cfapps.io:1002/hello](https://cfapps.io:1002/hello)  \n" +
                "Process [PID=90000, name=`test-request-mapping-live-hover`]");
    }

    @Test
    void testHighlights() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/RestApi.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("999")
                .processID("76543")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/delete/{id}],methods=[DELETE]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.removeMe(int)\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/postHello],methods=[POST]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.postMethod(java.lang.String)\"},\"{[/put/{id}],methods=[PUT]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.putMethod(int,java.lang.String)\"},\"{[/person/{name}],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.getMapping(java.lang.String)\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"},\"{[/application/status],methods=[GET],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}\":{\"bean\":\"webEndpointServletHandlerMapping\",\"method\":\"public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)\"},\"{[/application/info],methods=[GET],produces=[application/vnd.spring-boot.actuator.v2+json || application/json]}\":{\"bean\":\"webEndpointServletHandlerMapping\",\"method\":\"public java.lang.Object org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping$OperationHandler.handle(javax.servlet.http.HttpServletRequest,java.util.Map<java.lang.String, java.lang.String>)\"},\"{[/application],methods=[GET]}\":{\"bean\":\"webEndpointServletHandlerMapping\",\"method\":\"private java.util.Map<java.lang.String, java.util.Map<java.lang.String, org.springframework.boot.actuate.endpoint.web.Link>> org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping.links(javax.servlet.http.HttpServletRequest)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);

        editor.assertHighlights("@RequestMapping(\"/hello\")", "@RequestMapping(\"/goodbye\")",
                "@GetMapping(\"/person/{name}\")", "@DeleteMapping(\"/delete/{id}\")", "@PostMapping(\"/postHello\")",
                "@PutMapping(\"/put/{id}\")");

    }

    @Test
    void testMappingHoversInInnerClasses() throws Exception {

        File directory = new File(
                ProjectsHarness.class.getResource("/test-projects/test-request-mapping-live-hover/").toURI());
        String docUri = directory.toPath().resolve("src/main/java/example/InnerClassController.java").toUri()
                .toString();


        // Build a mock running boot app
        SpringProcessLiveData liveData = new SpringProcessLiveDataBuilder()
                .port("1111")
                .processID("22022")
                .urlScheme("https")
                .host("cfapps.io")
                .processName("test-request-mapping-live-hover")
                // Ugly, but this is real JSON copied from a real live running app. We want the
                // mock app to return realistic results if possible
                .requestMappingsJson(
                        "{\"/webjars/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**\":{\"bean\":\"resourceHandlerMapping\"},\"/**/favicon.ico\":{\"bean\":\"faviconHandlerMapping\"},\"{[/hello-world],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public example.Greeting example.HelloWorldController.sayHello(java.lang.String)\"},\"{[/inner-inner-class]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.InnerClassController$InnerController$InnerInnerController.saySomethingSuperInnerClass()\"},\"{[/inner-class]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.InnerClassController$InnerController.saySomething()\"},\"{[/person/{name}],methods=[GET]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.getMapping(java.lang.String)\"},\"{[/delete/{id}],methods=[DELETE]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.removeMe(int)\"},\"{[/goodbye]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.goodbye()\"},\"{[/hello]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.hello()\"},\"{[/postHello],methods=[POST]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.postMethod(java.lang.String)\"},\"{[/put/{id}],methods=[PUT]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public java.lang.String example.RestApi.putMethod(int,java.lang.String)\"},\"{[/error]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.http.ResponseEntity<java.util.Map<java.lang.String, java.lang.Object>> org.springframework.boot.autoconfigure.web.BasicErrorController.error(javax.servlet.http.HttpServletRequest)\"},\"{[/error],produces=[text/html]}\":{\"bean\":\"requestMappingHandlerMapping\",\"method\":\"public org.springframework.web.servlet.ModelAndView org.springframework.boot.autoconfigure.web.BasicErrorController.errorHtml(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse)\"}}")
                .build();
        harness.intialize(directory);
        liveDataProvider.add("processkey", liveData);

        Editor editor = harness.newEditorFromFileUri(docUri, LanguageId.JAVA);
        editor.assertHighlights("@RequestMapping(\"/inner-class\")", "@RequestMapping(\"/inner-inner-class\")");

        editor.assertHoverContains("@RequestMapping(\"/inner-class\")", "[https://cfapps.io:1111/inner-class](https://cfapps.io:1111/inner-class)  \n" +
                "Process [PID=22022, name=`test-request-mapping-live-hover`]");

        editor.assertHoverContains("@RequestMapping(\"/inner-inner-class\")", "[https://cfapps.io:1111/inner-inner-class](https://cfapps.io:1111/inner-inner-class)  \n" +
                "Process [PID=22022, name=`test-request-mapping-live-hover`]");
    }

}
