/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.test;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.lsp4j.CompletionItem;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Martin Lippert
 */
@RunWith(SpringRunner.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class DataRepositoryCompletionProcessorTest {

	@Autowired private BootLanguageServerHarness harness;
	private Editor editor;

	@Before
	public void setup() throws Exception {
		IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-spring-data-symbols");
		harness.useProject(testProject);
		harness.intialize(null);
	}

	@Test
	public void testStandardFindByCompletions() throws Exception {
		prepareCase("{", "{<*>");
		assertContainsAnnotationCompletions(
				"List<Customer> findByFirstName(String firstName);",
				"List<Customer> findByLastName(String lastName);");
	}

	private void prepareCase(String selectedAnnotation, String annotationStatementBeforeTest) throws Exception {
		InputStream resource = this.getClass().getResourceAsStream("/test-projects/test-spring-data-symbols/src/main/java/org/test/TestCustomerRepositoryForCompletions.java");
		String content = IOUtils.toString(resource);

		content = content.replace(selectedAnnotation, annotationStatementBeforeTest);
		editor = new Editor(harness, content, LanguageId.JAVA);
	}

	private void assertContainsAnnotationCompletions(String... expectedResultsFromCompletion) throws Exception {
		List<CompletionItem> completions = editor.getCompletions();

		int i = 0;
		for (CompletionItem foundCompletion : completions) {
			Editor clonedEditor = editor.clone();
			clonedEditor.apply(foundCompletion);

			if (clonedEditor.getText().contains(expectedResultsFromCompletion[i])) {
				i++;
			}
		}

		assertEquals(expectedResultsFromCompletion.length, i);
	}


}
