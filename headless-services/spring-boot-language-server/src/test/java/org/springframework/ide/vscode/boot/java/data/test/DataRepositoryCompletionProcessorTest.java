/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.test;

import java.io.InputStream;
import java.util.Arrays;
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

/**
 * @author Martin Lippert
 * @author danthe1st
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(HoverTestConf.class)
public class DataRepositoryCompletionProcessorTest {

	@Autowired private BootLanguageServerHarness harness;
	private Editor editor;

	@BeforeEach
	public void setup() throws Exception {
		IJavaProject testProject = ProjectsHarness.INSTANCE.mavenProject("test-spring-data-symbols");
		harness.useProject(testProject);
		harness.intialize(null);
	}

    @Test
    void testStandardFindByCompletions() throws Exception {
        prepareCase("{", "{<*>");
        assertStandardCompletions();
    }

    @Test
    void testPrefixSensitiveCompletionsNoPrefix() throws Exception {
    	prepareCase("{\n}", "{\n<*>");
    	assertStandardCompletions();
    }

	private void assertStandardCompletions() throws Exception {
		assertContainsAnnotationCompletions(
        		"countBy",
        		"deleteBy",
        		"existsBy",
        		"findBy",
                "List<Customer> findByFirstName(String firstName);",
                "List<Customer> findById(Long id);",
                "List<Customer> findByLastName(String lastName);",
                "List<Customer> findByResponsibleEmployee(Employee responsibleEmployee);",
                "getBy",
                "queryBy",
                "readBy",
                "removeBy",
                "searchBy",
                "streamBy");
	}

    @Test
    void testPrefixSensitiveCompletionsCompleteMethod() throws Exception {
    	checkCompletions("findByFirstNameAndLastName", "List<Customer> findByFirstNameAndLastName(String firstName, String lastName);");
    }

    @Test
    void testPrefixSensitiveCompletionsCompleteMethodReturnTypePresent() throws Exception {
    	checkCompletions("List<Customer> findByFirstNameAndLastName", "List<Customer> findByFirstNameAndLastName(String firstName, String lastName);");
    	checkCompletions("boolean existsByFirstNameAndLastName", "boolean existsByFirstNameAndLastName(String firstName, String lastName);");
    }

    @Test
    void testAttributeComparison() throws Exception {
    	checkCompletions("findByFirstNameIsGreaterThanLastName", "List<Customer> findByFirstNameIsGreaterThanLastName();");
    	checkCompletions("findByFirstNameIsLastName", "List<Customer> findByFirstNameIsLastName();");
    }

    @Test
    void testTerminatingKeyword() throws Exception {
    	checkCompletions("findByFirstNameNull", "List<Customer> findByFirstNameNull();");
    	checkCompletions("findByFirstNameNotNull", "List<Customer> findByFirstNameNotNull();");
    }

    @Test
    void testNewConditionAfterTerminatedExpression() throws Exception {
    	checkCompletions("findByFirstNameNullAndLastName", "List<Customer> findByFirstNameNullAndLastName(String lastName);");
    	checkCompletions("findByNotFirstNameNullAndNotLastName", "List<Customer> findByNotFirstNameNullAndNotLastName(String lastName);");
    }

    @Test
    void testDifferentSubjectTypes() throws Exception {
    	checkCompletions("existsByFirstName", "boolean existsByFirstName(String firstName);");
    	checkCompletions("countByFirstName", "long countByFirstName(String firstName);");
    	checkCompletions("streamByFirstName", "Streamable<Customer> streamByFirstName(String firstName);");
    	checkCompletions("removeByFirstName", "void removeByFirstName(String firstName);");
    }

    @Test
    void testUnknownAttribute() throws Exception {
    	checkCompletions("findByUnknownObject", "List<Customer> findByUnknownObject(Object unknownObject);");
    }

    @Test
    void testKeywordInPredicate() throws Exception {
    	checkCompletions("findByThisCustomerIsSpecial", "List<Customer> findByThisCustomerIsSpecial(boolean thisCustomerIsSpecial);");
    }

    @Test
    void testPropertyProposals() throws Exception {
    	checkCompletions("findByFirst", "findByFirstName");
    	checkCompletions("findByFirstNameAndL", "findByFirstNameAndLastName");
    	checkCompletions("findBy",
    			"findByFirstName",
    			"findByLastName");
    }

    @Test
    void findByComplexExpression() throws Exception {
    	checkCompletions("findByResponsibleEmployee", "List<Customer> findByResponsibleEmployee(Employee responsibleEmployee);");
    	checkCompletions("findByResponsibleEmployee_SocialSecurityNumber", "List<Customer> findByResponsibleEmployee_SocialSecurityNumber(Long responsibleEmployee_SocialSecurityNumber);");
    }

	private void checkCompletions(String alredyPresent, String... expectedCompletions) throws Exception {
		prepareCase("{\n}", "{\n\t" + alredyPresent + "<*>");
    	assertContainsAnnotationCompletions(Arrays.stream(expectedCompletions).map(expected -> "\t" + expected + "<*>").toArray(String[]::new));
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

			if (i < expectedResultsFromCompletion.length && clonedEditor.getText().contains(expectedResultsFromCompletion[i])) {
				i++;
			}
		}
		assertEquals(expectedResultsFromCompletion.length, i);
	}
}
