/*******************************************************************************
 * Copyright (c) 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping.test;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.HoverTestConf;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class RequestMappingSnippetTests {
	
	@Autowired private BootLanguageServerHarness harness;
	private Editor editor;

	@BeforeEach
	public void setup() throws Exception {
		IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-request-mapping-live-hover");
		harness.useProject(testProject);
		harness.intialize(null);
	}

    @Test
    void getMapping() throws Exception {
        prepareCase("Get<*>");
        assertOneSnippet("package example;\n"
                + "\n"
                + "import org.springframework.stereotype.Controller;\n"
                + "import org.springframework.web.bind.annotation.DeleteMapping;\n"
                + "import org.springframework.web.bind.annotation.GetMapping;\n"
                + "import org.springframework.web.bind.annotation.PathVariable;\n"
                + "import org.springframework.web.bind.annotation.PostMapping;\n"
                + "import org.springframework.web.bind.annotation.PutMapping;\n"
                + "import org.springframework.web.bind.annotation.RequestBody;\n"
                + "import org.springframework.web.bind.annotation.RequestMapping;\n"
                + "import org.springframework.web.bind.annotation.ResponseBody;\n"
                + "\n"
                + "/** Boot Java - Test Completion */\n"
                + "@Controller\n"
                + "public class RestApi {\n"
                + "\n"
                + "@GetMapping(value=\"${1:path}\")\n"
                + "public ${2:SomeData} ${3:getMethodName}(@RequestParam ${4:String} ${5:param}) {\n"
                + "    return new ${2:SomeData}($0);\n"
                + "}\n"
                + "<*>\n"
                + "\n"
                + "\n"
                + "	@RequestMapping(\"/hello\")\n"
                + "	@ResponseBody\n"
                + "	public String hello() {\n"
                + "		return \"Hello there!\";\n"
                + "	}\n"
                + "	\n"
                + "	\n"
                + "	@RequestMapping(\"/goodbye\")\n"
                + "	@ResponseBody\n"
                + "	public String goodbye() {\n"
                + "		return \"Good bye\";\n"
                + "	}\n"
                + "\n"
                + "	@GetMapping(\"/person/{name}\")\n"
                + "	public String getMapping(@PathVariable String name) {\n"
                + "		return \"Hello \" + name;\n"
                + "	}\n"
                + "\n"
                + "	@DeleteMapping(\"/delete/{id}\")\n"
                + "	public String removeMe(@PathVariable int id) {\n"
                + "		System.out.println(\"You are removed: \" + id);\n"
                + "		return \"Done\";\n"
                + "	}\n"
                + "\n"
                + "	@PostMapping(\"/postHello\")\n"
                + "	public String postMethod(@RequestBody String name) {\n"
                + "		System.out.println(\"Posted hello: \" + name);\n"
                + "		return name;\n"
                + "	}\n"
                + "\n"
                + "	@PutMapping(\"/put/{id}\")\n"
                + "	public String putMethod(@PathVariable int id, @RequestBody String name) {\n"
                + "		System.out.println(\"Added \" + name + \" with ID: \" + id);\n"
                + "		return name;\n"
                + "	}\n"
                + "}\n"
                + "");
    }

	private void prepareCase(String prefix) throws Exception {
		InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-request-mapping-live-hover/src/main/java/example/RestApi.java");
		String content = IOUtils.toString(resource);

		content = content.replace("class RestApi {", "class RestApi {\n\n" + prefix);
		editor = new Editor(harness, content, LanguageId.JAVA);
	}
	
	private void assertOneSnippet(String expected) throws Exception {
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
		Editor clonedEditor = editor.clone();
		clonedEditor.apply(completions.get(0));
		assertEquals(expected, clonedEditor.getText());
	}

}
