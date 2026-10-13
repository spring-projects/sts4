/*******************************************************************************
 * Copyright (c) 2024, 2026 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.ImplicitWebAnnotationNamesReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class ImplicitWebAnnotationNamesReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "implicitwebannotationnames";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new ImplicitWebAnnotationNamesReconciler(new QuickfixRegistry());
	}
	
	@BeforeEach
	void setup() throws Exception {
		super.setup();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		super.tearDown();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"@ModelAttribute(\"form\") UserRegistrationForm form",
			"@ModelAttribute(value = \"form\") UserRegistrationForm form",
			"@ModelAttribute(name = \"form\") UserRegistrationForm form",
			"@ModelAttribute(name = \"form\", binding = false) UserRegistrationForm form",
			"@ModelAttribute(\"registration\") UserRegistrationForm form",
			"@ModelAttribute UserRegistrationForm form",
			"@ModelAttribute(binding = false) UserRegistrationForm form",
			"@ModelAttribute(\"fo\" + \"rm\") UserRegistrationForm form",
			"@ModelAttribute(\"userRegistrationForm\") UserRegistrationForm form",
			"@ModelAttribute(\"userRegistrationForm\") UserRegistrationForm userRegistrationForm",
			"@org.springframework.web.bind.annotation.ModelAttribute(\"form\") UserRegistrationForm form",
			"@org.springframework.web.bind.annotation.ModelAttribute(value = \"form\") UserRegistrationForm form",
			"@org.springframework.web.bind.annotation.ModelAttribute(name = \"form\") UserRegistrationForm form"
	})
	void modelAttributeNamesAreNotParameterNames(String parameter) throws Exception {
		String source = """
				package example.demo;

				import org.springframework.stereotype.Controller;
				import org.springframework.validation.BindingResult;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class A {
					@PostMapping("/register")
					public String register(%s, BindingResult result) {
						return "success";
					}
				}

				class UserRegistrationForm {}
				""".formatted(parameter);

		// ModelAttribute is intentionally excluded, even when its name matches the type-derived default.
		assertEquals(0, reconcile("A.java", source, false).size());
	}

	@Test
	void mixedWebAnnotationsRetainOnlyParameterNameQuickfixes() throws Exception {
		String source = """
				package example.demo;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.*;

				@Controller
				class A {
					@PostMapping("/register/{userId}")
					public String register(@ModelAttribute("form") UserRegistrationForm form,
							@RequestParam("id") String id,
							@PathVariable("userId") String userId,
							@RequestHeader(value = "header") String header,
							@RequestAttribute(name = "attribute") String attribute,
							@CookieValue("cookie") String cookie,
							@SessionAttribute("session") String session,
							@FormParam("extra") String extra) {
						return "success";
					}

					@GetMapping("/user")
					public String user(@RequestParam("user_id") String userId) {
						return userId;
					}
				}

				class UserRegistrationForm {}
				@interface FormParam { String value(); }
				""";

		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		assertEquals(List.of("\"id\"", "\"userId\"", "value = \"header\"", "name = \"attribute\"", "\"cookie\"", "\"session\""),
				problems.stream().map(p -> source.substring(p.getOffset(), p.getOffset() + p.getLength())).toList());
		for (ReconcileProblem problem : problems) {
			assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
			List<FixDescriptor> fixes = problem.getQuickfixes().stream().map(f -> (FixDescriptor) f.params).toList();
			assertEquals(List.of(RecipeScope.NODE, RecipeScope.FILE, RecipeScope.PROJECT),
					fixes.stream().map(FixDescriptor::getRecipeScope).toList());
			for (FixDescriptor fix : fixes) {
				assertEquals(org.openrewrite.java.spring.ImplicitWebAnnotationNames.class.getName(), fix.getRecipeId());
			}
		}
	}
	
	@Test
	void sanityTest() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@PathVariable("message") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("\"message\"", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		assertEquals("Remove implicit web annotation name", problems.get(0).getQuickfixes().get(0).title);
		assertEquals("Remove implicit web annotation names in file", problems.get(0).getQuickfixes().get(1).title);
		assertEquals("Remove implicit web annotation names in project", problems.get(0).getQuickfixes().get(2).title);
		
	}
	
	@Test
	void webAnnotationNameFromConcatenatedString() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@PathVariable("mess" + "age") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("\"mess\" + \"age\"", markedStr);
	}
	
	@Test
	void webAnnotationWithDifferentNames() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{msg}")
					public String hello(@PathVariable("msg") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}
	
	@Test
	void webAnnotationWithAssignment() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@PathVariable(value="message") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("value=\"message\"", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		
	}
	
	@Test
	void webAnnotationWithMultipleParams() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.RequestParam;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}")
					public String hello(@RequestParam(name="message", defaultValue = "world") String message) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("name=\"message\"", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		
	}
	
	@Test
	void multipleWebAnnotations() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.GetMapping;
				import org.springframework.web.bind.annotation.PathVariable;
				
				@Controller
				class A {
				
					@GetMapping("/hello/{message}/{name}/{address}")
					public String hello(@PathVariable(value="message", required=false) String message, @PathVariable(value="name") String name, @PathVariable(value="address", required=false) String location) {
						return "Hello "+ message; 
					}
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(2, problems.size());
		
		ReconcileProblem problem1 = problems.get(0);
		ReconcileProblem problem2 = problems.get(1);
		
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem1.getType());
		assertEquals(Boot2JavaProblemType.WEB_ANNOTATION_NAMES, problem2.getType());
		
		String markedStr1 = source.substring(problem1.getOffset(), problem1.getOffset() + problem1.getLength());
		assertEquals("value=\"message\"", markedStr1);
		
		String markedStr2 = source.substring(problem2.getOffset(), problem2.getOffset() + problem2.getLength());
		assertEquals("value=\"name\"", markedStr2);

		assertEquals(3, problem1.getQuickfixes().size());
		assertEquals(3, problem2.getQuickfixes().size());
		
	}

}
