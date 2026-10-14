/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.JdtFixDescriptor;
import org.springframework.ide.vscode.boot.java.reconcilers.ImplicitModelAttributeNamesReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Tests for {@link ImplicitModelAttributeNamesReconciler}
 *
 * @author Nihal Kumar Sharma
 */
public class ImplicitModelAttributeNamesReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "implicitmodelattributenames";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new ImplicitModelAttributeNamesReconciler(new QuickfixRegistry());
	}

	@BeforeEach
	void setup() throws Exception {
		super.setup();
	}

	@AfterEach
	void tearDown() throws Exception {
		super.tearDown();
	}

	private static String controller(String parameter) {
		return """
				package example.demo;

				import java.util.ArrayList;
				import java.util.List;
				import java.util.Optional;
				import java.util.concurrent.CompletableFuture;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class A {
					@PostMapping("/save")
					public String save(%s) {
						return "success";
					}
				}

				class User {}
				class URLInfo {}
				class Outer {
					static class Inner {}
				}
				record SignupForm(String email) {}
				class Order {}
				class Orders extends ArrayList<Order> {}
				class UserFuture extends CompletableFuture<User> {}
				class Incomplete extends MissingBase {}
				class Names {
					static final String USER = "user";
				}
				""".formatted(parameter);
	}

	private static String applyFix(String source, JdtFixDescriptor fix) throws Exception {
		// the quickfix re-checks the name through resolved bindings before removing it
		String springWebJar = new File(ModelAttribute.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();

		ASTParser parser = ASTParser.newParser(AST.JLS25);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setEnvironment(new String[] { springWebJar }, new String[0], null, true);
		parser.setUnitName("A.java");
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_21, options);
		parser.setCompilerOptions(options);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
		fix.refactoring().apply(rewrite, cu);
		Document doc = new Document(source);
		rewrite.rewriteAST(doc, options).apply(doc);
		return doc.get();
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"@ModelAttribute(\"user\") User account",
			"@ModelAttribute(value = \"user\") User user",
			"@ModelAttribute(name = \"user\") User user",
			"@ModelAttribute(name = \"user\", binding = false) User user",
			"@org.springframework.web.bind.annotation.ModelAttribute(\"user\") User user",
			"@ModelAttribute(\"URLInfo\") URLInfo info",
			"@ModelAttribute(\"inner\") Outer.Inner value",
			"@ModelAttribute(\"signupForm\") SignupForm form"
	})
	void nameDerivedFromTypeIsFlagged(String parameter) throws Exception {
		List<ReconcileProblem> problems = reconcile("A.java", controller(parameter), false);

		assertEquals(1, problems.size());
		assertEquals(Boot2JavaProblemType.MODEL_ATTRIBUTE_NAMES, problems.get(0).getType());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"@ModelAttribute(\"account\") User account",
			"@ModelAttribute(\"uRLInfo\") URLInfo info",
			"@ModelAttribute User user",
			"@ModelAttribute(binding = false) User user",
			"@ModelAttribute(\"us\" + \"er\") User user",
			"@ModelAttribute(Names.USER) User user",
			"@ModelAttribute(value = \"user\", name = \"user\") User user",
			"@ModelAttribute(\"orderList\") List<Order> orders",
			"@ModelAttribute(\"orderList\") Order[] orders",
			"@ModelAttribute(\"order\") Order... orders",
			"@ModelAttribute(\"user\") Optional<User> user",
			"@ModelAttribute(\"optional\") Optional<User> user",
			"@ModelAttribute(\"orders\") Orders orders",
			"@ModelAttribute(\"userFuture\") UserFuture user",
			"@ModelAttribute(\"missing\") Missing missing",
			"@ModelAttribute(\"incomplete\") Incomplete incomplete"
	})
	void nameNotDerivedFromTypeIsNotFlagged(String parameter) throws Exception {
		assertEquals(0, reconcile("A.java", controller(parameter), false).size());
	}

	@Test
	void sanityTest() throws Exception {
		String source = controller("@ModelAttribute(\"user\") User account");

		List<ReconcileProblem> problems = reconcile("A.java", source, false);

		assertEquals(1, problems.size());
		ReconcileProblem problem = problems.get(0);
		assertEquals(Boot2JavaProblemType.MODEL_ATTRIBUTE_NAMES, problem.getType());
		assertEquals("\"user\"", source.substring(problem.getOffset(), problem.getOffset() + problem.getLength()));

		assertEquals(2, problem.getQuickfixes().size());
		assertEquals("Remove implicit `@ModelAttribute` name", problem.getQuickfixes().get(0).title);
		assertEquals("Remove implicit `@ModelAttribute` names in file", problem.getQuickfixes().get(1).title);

		JdtFixDescriptor fix = (JdtFixDescriptor) problem.getQuickfixes().get(0).params;
		assertEquals(controller("@ModelAttribute User account"), applyFix(source, fix));
	}

	@Test
	void otherAttributesSurviveQuickfix() throws Exception {
		String source = controller("@ModelAttribute(name = \"user\", binding = false) User account");

		List<ReconcileProblem> problems = reconcile("A.java", source, false);

		assertEquals(1, problems.size());
		ReconcileProblem problem = problems.get(0);
		assertEquals("name = \"user\"", source.substring(problem.getOffset(), problem.getOffset() + problem.getLength()));

		JdtFixDescriptor fix = (JdtFixDescriptor) problem.getQuickfixes().get(0).params;
		assertEquals(controller("@ModelAttribute(binding = false) User account"), applyFix(source, fix));
	}

	@Test
	void fixAllInFileKeepsMeaningfulNames() throws Exception {
		String source = """
				package example.demo;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class A {
					@PostMapping("/users")
					public String saveUser(@ModelAttribute("user") User user, @ModelAttribute("account") User account) {
						return "success";
					}

					@PostMapping("/orders")
					public String saveOrder(@ModelAttribute(value = "order", binding = false) Order order) {
						return "success";
					}
				}

				class User {}
				class Order {}
				""";

		List<ReconcileProblem> problems = reconcile("A.java", source, false);

		assertEquals(2, problems.size());
		JdtFixDescriptor fixAll = (JdtFixDescriptor) problems.get(0).getQuickfixes().get(1).params;
		assertEquals(source
				.replace("@ModelAttribute(\"user\")", "@ModelAttribute")
				.replace("@ModelAttribute(value = \"order\", binding = false)", "@ModelAttribute(binding = false)"),
				applyFix(source, fixAll));
	}

	@Test
	void methodLevelModelAttributeIsNotFlagged() throws Exception {
		String source = """
				package example.demo;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;

				@Controller
				class A {
					@ModelAttribute("user")
					public User user() {
						return new User();
					}
				}

				class User {}
				""";

		assertEquals(0, reconcile("A.java", source, false).size());
	}

	@Test
	void otherWebAnnotationsAreNotFlagged() throws Exception {
		String source = """
				package example.demo;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.PostMapping;
				import org.springframework.web.bind.annotation.RequestAttribute;
				import org.springframework.web.bind.annotation.SessionAttribute;

				@Controller
				class A {
					@PostMapping("/save")
					public String save(@RequestAttribute("user") User user, @SessionAttribute("user") User sessionUser) {
						return "success";
					}
				}

				class User {}
				""";

		assertEquals(0, reconcile("A.java", source, false).size());
	}

}
