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
import static org.junit.jupiter.api.Assertions.fail;

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
	void assertCompletionWithImportDifferentAndSamePackage() throws Exception {
		checkCompletionResult("", "findByResponsibleEmployee", """
				package org.test;

				import org.springframework.data.repository.CrudRepository;
				import java.util.List;
				import org.test.model.Employee;


				public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
					List<Customer> findByResponsibleEmployee(Employee responsibleEmployee);<*>
				}
				""");
	}

	@Test
	void assertCompletionWithImportFromJavaLang() throws Exception {
		checkCompletionResult("", "findById", """
				package org.test;

				import org.springframework.data.repository.CrudRepository;
				import java.util.List;


				public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
					List<Customer> findById(Long id);<*>
				}
				""");
	}
	
	@Test
	void prefixSensitiveMethodCompletionWithImports_1() throws Exception {
		checkCompletionResult("findByResponsibleEmployeeAndLastName", "findByResponsibleEmployeeAndLastName", """
				package org.test;

				import org.springframework.data.repository.CrudRepository;
				import org.test.model.Employee;
				import java.util.List;


				public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
					List<Customer> findByResponsibleEmployeeAndLastName(Employee responsibleEmployee, String lastName);<*>
				}
				""");
	}
	
	@Test
	void prefixSensitiveMethodCompletionWithImports_2() throws Exception {
		checkCompletionResult("streamByResponsibleEmployeeAndLastName", "streamByResponsibleEmployeeAndLastName", """
				package org.test;

				import org.springframework.data.repository.CrudRepository;
				import org.test.model.Employee;
				import org.springframework.data.util.Streamable;


				public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
					Streamable<Customer> streamByResponsibleEmployeeAndLastName(Employee responsibleEmployee, String lastName);<*>
				}
				""");
	}
	
	@Test
	void prefixSensitiveMethodCompletionWithImports_3() throws Exception {
		checkCompletionResult("countByResponsibleEmployee", "countByResponsibleEmployee", """
				package org.test;

				import org.springframework.data.repository.CrudRepository;
				import org.test.model.Employee;


				public interface TestCustomerRepositoryForCompletions extends CrudRepository<Customer, Long> {
					long countByResponsibleEmployee(Employee responsibleEmployee);<*>
				}
				""");
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
    	checkCompletions("findByUnknownObject");
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
    void testFindByComplexExpression() throws Exception {
    	checkCompletions("findByResponsibleEmployee", "List<Customer> findByResponsibleEmployee(Employee responsibleEmployee);");
    	checkCompletions("findByResponsibleEmployee_SocialSecurityNumber", "List<Customer> findByResponsibleEmployee_SocialSecurityNumber(Long responsibleEmployee_SocialSecurityNumber);");
    }

    @Test
    void testAppendKeywords() throws Exception {
    	checkCompletions("findByFirstName",
    			"findByFirstNameAnd",
    			"findByFirstNameExists",
    			"findByFirstNameIgnoreCase",
    			"findByFirstNameIsLessThanEqual",
    			"findByFirstNameOr",
    			"findByFirstNameOrderBy");
    }

    @Test
    void testAppendKeywordsWithPreviousKeyword() throws Exception {
    	checkCompletions("findByFirstNameAnd",
    			"findByFirstNameAndNot");
    }

    @Test
    void testAppendKeywordsStartAlreadyPresent() throws Exception {
    	checkCompletions("findByFirstNameA",
    			"findByFirstNameAfter",
    			"findByFirstNameAnd");
    }
    
    private void checkCompletionResult(String prefix, String completionLabel, String result) throws Exception {
		prepareCase("{\n}", "{\n\t" + prefix + "<*>\n}");
		List<CompletionItem> completions = editor.getCompletions();

		int i = 0;
		for (CompletionItem foundCompletion : completions) {
			if (foundCompletion.getLabel().contains(completionLabel)) {
				Editor clonedEditor = editor.clone();
				clonedEditor.apply(foundCompletion);
				assertEquals(result, clonedEditor.getText());
				return;
			}
		}
		fail("Didn't find the proposal with label: " + completionLabel);
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
