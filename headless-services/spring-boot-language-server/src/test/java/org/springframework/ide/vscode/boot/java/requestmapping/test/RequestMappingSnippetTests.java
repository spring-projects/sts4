/*******************************************************************************
 * Copyright (c) 2022, 2023 Pivotal, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.InsertTextMode;
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
	
	private static final String CONTROLLER_CLASSNAME = "SampleController";
	private static final String CONTROLLER_WITH_EXISTING_CODE_CLASSNAME = "SampleControllerWithExistingCode";
	private static final String CONTROLLER_WITH_RANDOM_CODE_CLASSNAME = "SampleControllerWithRandomCode";
	private static final String NON_CONTROLLER_CLASSNAME = "SampleNonController";
	
	@Autowired private BootLanguageServerHarness harness;
	private Editor editor;

	@BeforeEach
	public void setup() throws Exception {
		IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-request-mapping-completions");
		harness.useProject(testProject);
		harness.intialize(null);
	}
	
    @Test
    void testAnnotationMarkerOnlyPrefix() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "@<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(4, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("@PostMapping(..) {..}", completions.get(1).getLabel());
        assertEquals("@PutMapping(..) {..}", completions.get(2).getLabel());
        assertEquals("@RequestMapping(..) {..}", completions.get(3).getLabel());
        
        assertEquals(InsertTextMode.AsIs, completions.get(0).getInsertTextMode());
        assertEquals(InsertTextMode.AsIs, completions.get(1).getInsertTextMode());
        assertEquals(InsertTextMode.AsIs, completions.get(2).getInsertTextMode());
        assertEquals(InsertTextMode.AsIs, completions.get(3).getInsertTextMode());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getFilterText());
        assertEquals("@PostMapping(..) {..}", completions.get(1).getFilterText());
        assertEquals("@PutMapping(..) {..}", completions.get(2).getFilterText());
        assertEquals("@RequestMapping(..) {..}", completions.get(3).getFilterText());
    }

    @Test
    void testAnnotationMarkerOnlyPrefixWithExistingCode() throws Exception {
        prepareCase(CONTROLLER_WITH_EXISTING_CODE_CLASSNAME, "// <*>", "@G<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("@GetMapping(..) {..}", completions.get(0).getFilterText());
    }

    @Test
    void testSimpleGetPrefix() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "Get<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("GetMapping", completions.get(0).getFilterText());
    }

    @Test
    void testSimplePostPrefix() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "Post<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@PostMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("PostMapping", completions.get(0).getFilterText());
    }

    @Test
    void testAnnotationAndTextPrefixForGet() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "@G<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("@GetMapping(..) {..}", completions.get(0).getFilterText());
    }

    @Test
    void testAnnotationAndTextPrefixForGet2() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "@Get<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("@GetMapping(..) {..}", completions.get(0).getFilterText());
    }

    @Test
    void testAnnotationAndTextPrefixForGet3() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "@GetM<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());
        
        assertEquals("@GetMapping(..) {..}", completions.get(0).getLabel());
        assertEquals("@GetMapping(..) {..}", completions.get(0).getFilterText());
    }

    @Test
    void testSnippetExtractionIntoCode() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "Get<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(1, completions.size());

        assertSnippets(completions, "package example;\n"
        		+ "\n"
        		+ "import org.springframework.stereotype.Controller;\n"
        		+ "import org.springframework.web.bind.annotation.GetMapping;\n"
        		+ "import org.springframework.web.bind.annotation.RequestParam;\n"
        		+ "\n"
        		+ "\n"
        		+ "@Controller\n"
        		+ "public class SampleController {\n"
        		+ "\n"
        		+ "@GetMapping(\"${1:path}\")\n"
        		+ "public ${2:String} ${3:getMethodName}(@RequestParam ${4:String} ${5:param}) {\n"
        		+ "    return new ${2:String}($0);\n"
        		+ "}\n"
        		+ "<*>\n"
        		+ "\n"
        		+ "}\n"
        		+ "");
    }
    
    @Test
    void testSnippetsNotShowUpForNonControllerClasses() throws Exception {
        prepareCase(NON_CONTROLLER_CLASSNAME, "@<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
        assertEquals(0, completions.size());
    }
    
    @Test
    void testSnippetNotShowUpForPrefixInMethodName() throws Exception {
        prepareCase(CONTROLLER_WITH_RANDOM_CODE_CLASSNAME, "getSomethingMethod", "get<*>SomethingMethod");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }
    
    @Test
    void testSnippetNotShowUpForPrefixInAnnotationPropertyValue() throws Exception {
        prepareCase(CONTROLLER_WITH_RANDOM_CODE_CLASSNAME, "@Value(\"${getValueProperty}\")", "@Value(\"${get<*>ValueProperty}\")");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }
    
    @Test
    void testSnippetNotShowUpForPrefixInMethodReturnType() throws Exception {
        prepareCase(CONTROLLER_WITH_RANDOM_CODE_CLASSNAME, "public GetSomeService getSomethingMethod", "public Get<*>SomeService getSomethingMethod");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }
    
    @Test
    void testSnippetNotShowUpWithinMethodBody() throws Exception {
        prepareCase(CONTROLLER_WITH_RANDOM_CODE_CLASSNAME, "return new GetSomeService();", "get<*>\n    return new GetSomeService();");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }
    
    @Test
    void testSnippetNotShowUpWithinMethodParamDeclaration() throws Exception {
        prepareCase(CONTROLLER_WITH_RANDOM_CODE_CLASSNAME, "@RequestParam(value = \"name\"", "@Get<*> @RequestParam(value = \"name\"");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }
    
    @Test
    void testSnippetNotShowUpOnClassDeclaration() throws Exception {
        prepareCase(CONTROLLER_CLASSNAME, "@Controller", "@Controller\n<*>");
        
		List<CompletionItem> completions = editor.getCompletions();
		for (CompletionItem completionItem : completions) {
	        assertNotEquals("@GetMapping(..) {..}", completionItem.getLabel());
		}
    }

    private void prepareCase(String className, String prefix) throws Exception {
    	prepareCase(className, "class " + className + " {", "class " + className + " {\n\n" + prefix);
    }

	private void prepareCase(String className, String replace, String replaceWith) throws Exception {
		InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-request-mapping-completions/src/main/java/example/" + className + ".java");
		String content = IOUtils.toString(resource, Charset.defaultCharset());

		content = content.replace(replace, replaceWith);
		editor = new Editor(harness, content, LanguageId.JAVA);
	}
	
	private void assertSnippets(List<CompletionItem> completions, String... expected) throws Exception {
        for (int i = 0; i < expected.length; i++) {
    		Editor clonedEditor = editor.clone();
    		clonedEditor.apply(completions.get(i));
    		assertEquals(expected[i], clonedEditor.getText());
        }
	}

}
