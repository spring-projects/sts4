/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.scope.test;

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
import org.springframework.ide.vscode.languageserver.testharness.TestAsserts;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author Martin Lippert
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class ScopeCompletionTest {

	@Autowired private BootLanguageServerHarness harness;
	private Editor editor;

	@BeforeEach
	public void setup() throws Exception {
		IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-annotations");
		harness.useProject(testProject);
		harness.intialize(null);
	}

//	private IJavaProject getTestProject() {
//		return testProject;
//	}

    @Test
    void testEmptyBracketsCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(<*>)");
        assertAnnotationCompletions(
                "@Scope(\"application\"<*>)",
                "@Scope(\"globalSession\"<*>)",
                "@Scope(\"prototype\"<*>)",
                "@Scope(\"request\"<*>)",
                "@Scope(\"session\"<*>)",
                "@Scope(\"singleton\"<*>)",
                "@Scope(\"websocket\"<*>)");
    }

    @Test
    void testEmptyStringLiteralCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(\"<*>\")");
        assertAnnotationCompletions(
                "@Scope(\"application\"<*>)",
                "@Scope(\"globalSession\"<*>)",
                "@Scope(\"prototype\"<*>)",
                "@Scope(\"request\"<*>)",
                "@Scope(\"session\"<*>)",
                "@Scope(\"singleton\"<*>)",
                "@Scope(\"websocket\"<*>)");
    }

    @Test
    void testEmptyValueCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(value=<*>)");
        assertAnnotationCompletions(
                "@Scope(value=\"application\"<*>)",
                "@Scope(value=\"globalSession\"<*>)",
                "@Scope(value=\"prototype\"<*>)",
                "@Scope(value=\"request\"<*>)",
                "@Scope(value=\"session\"<*>)",
                "@Scope(value=\"singleton\"<*>)",
                "@Scope(value=\"websocket\"<*>)");
    }

    @Test
    void testEmptyValueStringLiteralCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(value=\"<*>\")");
        assertAnnotationCompletions(
                "@Scope(value=\"application\"<*>)",
                "@Scope(value=\"globalSession\"<*>)",
                "@Scope(value=\"prototype\"<*>)",
                "@Scope(value=\"request\"<*>)",
                "@Scope(value=\"session\"<*>)",
                "@Scope(value=\"singleton\"<*>)",
                "@Scope(value=\"websocket\"<*>)");
    }

    @Test
    void testPrefixWithClosingQuotesCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(\"pro<*>\")");
        assertAnnotationCompletions(
                "@Scope(\"prototype\"<*>)");
    }

    @Test
    void testPrefixWithoutClosingQuotesCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(\"pro<*>)");
        assertAnnotationCompletions();
    }

    @Test
    void testValuePrefixWithClosingQuotesCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(value=\"pro<*>\")");
        assertAnnotationCompletions(
                "@Scope(value=\"prototype\"<*>)");
    }

    @Test
    void testValuePrefixWithoutClosingQuotesCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(value=\"pro<*>)");
        assertAnnotationCompletions();
    }

    @Test
    void testPrefixReplaceRestCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(\"pro<*>something\")");
        assertAnnotationCompletions(
                "@Scope(\"prototype\"<*>)");
    }

    @Test
    void testDifferentMemberNameCompletion() throws Exception {
        prepareCase("@Scope(\"onClass\")", "@Scope(proxyName=\"<*>\")");
        assertAnnotationCompletions();
    }

	private void prepareCase(String selectedAnnotation, String annotationStatementBeforeTest) throws Exception {
		InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-annotations/src/main/java/org/test/TestScopeCompletion.java");
		String content = IOUtils.toString(resource);

		content = content.replace(selectedAnnotation, annotationStatementBeforeTest);
		editor = new Editor(harness, content, LanguageId.JAVA);
	}

	private void assertAnnotationCompletions(String... completedAnnotations) throws Exception {
		List<CompletionItem> completions = editor.getCompletions();
		int i = 0;
		for (String expectedCompleted : completedAnnotations) {
			Editor clonedEditor = editor.clone();
			clonedEditor.apply(completions.get(i++));
			TestAsserts.assertContains(expectedCompleted, clonedEditor.getText());
		}

		assertEquals(i, completions.size());
	}


}
