/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.bootiful.BootLanguageServerTest;
import org.springframework.ide.vscode.boot.bootiful.IndexerTestConf;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.JdtRefactorings;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixResolveParams;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.languageserver.testharness.CodeAction;
import org.springframework.ide.vscode.languageserver.testharness.Editor;
import org.springframework.ide.vscode.project.harness.BootLanguageServerHarness;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Tests for the implicit {@code @ModelAttribute} names validation running in the language server,
 * including its quickfixes and its preference.
 *
 * @author Nihal Kumar Sharma
 */
@ExtendWith(SpringExtension.class)
@BootLanguageServerTest
@Import(IndexerTestConf.class)
public class ImplicitModelAttributeNamesQuickfixTest {

	@Autowired private BootLanguageServerHarness harness;
	@Autowired private JavaProjectFinder projectFinder;
	@Autowired private SpringSymbolIndex indexer;

	private File directory;

	@BeforeEach
	public void setup() throws Exception {
		harness.intialize(null);
		harness.changeConfiguration("{\"boot-java\": {\"validation\": {\"java\": { \"reconcilers\": true}}}}");
		directory = new File(ProjectsHarness.class.getResource("/test-projects/test-spring-validations/").toURI());
		String projectDir = directory.toURI().toString();
		projectFinder.find(new TextDocumentIdentifier(projectDir)).get();
		CompletableFuture<Void> initProject = indexer.waitOperation();
		initProject.get(5, TimeUnit.SECONDS);
	}

	private Editor newEditor(String source) throws Exception {
		String docUri = directory.toPath().resolve("src/main/java/org/test/UserController.java").toUri().toString();
		return harness.newEditor(LanguageId.JAVA, source, docUri);
	}

	@Test
	void quickfixRemovesRedundantName() throws Exception {
		Editor editor = newEditor("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/users")
					String save(@ModelAttribute("user") User account, @ModelAttribute("customer") User customer) {
						return "users";
					}
				}

				class User {}
				""");

		Diagnostic problem = editor.assertProblem("\"user\"");
		assertEquals(Boot2JavaProblemType.MODEL_ATTRIBUTE_NAMES.getCode(), problem.getCode().getLeft());

		List<CodeAction> codeActions = editor.getCodeActions(problem);
		assertEquals(2, codeActions.size());
		assertEquals("Remove implicit `@ModelAttribute` name", codeActions.get(0).getLabel());
		assertEquals("Remove implicit `@ModelAttribute` names in file", codeActions.get(1).getLabel());

		harness.executeCommand(codeActions.get(0).getCommand());

		assertEquals("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/users")
					String save(@ModelAttribute User account, @ModelAttribute("customer") User customer) {
						return "users";
					}
				}

				class User {}
				""", editor.getRawText());

		editor.assertProblems();
	}

	@Test
	void quickfixRemovesAllRedundantNamesInFile() throws Exception {
		Editor editor = newEditor("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/save")
					String save(@ModelAttribute("user") User user) {
						return "users";
					}

					@PostMapping("/preview")
					String preview(@ModelAttribute(name = "user", binding = false) User user) {
						return "preview";
					}
				}

				class User {}
				""");

		List<Diagnostic> problems = editor.assertProblems("\"user\"|`@ModelAttribute` name", "name = \"user\"|`@ModelAttribute` name");

		List<CodeAction> codeActions = editor.getCodeActions(problems.get(0));
		assertEquals("Remove implicit `@ModelAttribute` names in file", codeActions.get(1).getLabel());

		harness.executeCommand(codeActions.get(1).getCommand());

		assertEquals("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/save")
					String save(@ModelAttribute User user) {
						return "users";
					}

					@PostMapping("/preview")
					String preview(@ModelAttribute(binding = false) User user) {
						return "preview";
					}
				}

				class User {}
				""", editor.getRawText());

		editor.assertProblems();
	}

	@Test
	void outdatedQuickfixKeepsNameThatIsNoLongerRedundant() throws Exception {
		Editor editor = newEditor("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/users")
					String save(@ModelAttribute("user") User customer) {
						return "users";
					}
				}

				class User {}
				class Account {}
				""");

		Command quickfix = editor.getCodeActions(editor.assertProblem("\"user\"")).get(0).getCommand();

		// the parameter type changes before the quickfix computed for the previous content gets executed
		editor.setRawText(editor.getRawText().replace("User customer", "Account customer"));
		editor.assertProblems();

		// resolve the quickfix the same way executing its command does, it must not compute any edit
		QuickfixEdit edit = harness.getServer().quickfixResolve(
				new QuickfixResolveParams(JdtRefactorings.JDT_QUICKFIX, quickfix.getArguments().get(1))).block();
		assertNull(edit.workspaceEdit);
	}

	@Test
	void problemIsNotReportedWhenIgnored() throws Exception {
		harness.changeConfiguration("""
				{
					"boot-java": {"validation": {"java": {"reconcilers": true}}},
					"spring-boot": {"ls": {"problem": {"boot2": {"MODEL_ATTRIBUTE_NAMES": "IGNORE"}}}}
				}
				""");

		Editor editor = newEditor("""
				package org.test;

				import org.springframework.stereotype.Controller;
				import org.springframework.web.bind.annotation.ModelAttribute;
				import org.springframework.web.bind.annotation.PathVariable;
				import org.springframework.web.bind.annotation.PostMapping;

				@Controller
				class UserController {

					@PostMapping("/users/{id}")
					String save(@PathVariable("id") String id, @ModelAttribute("user") User user) {
						return "users";
					}
				}

				class User {}
				""");

		// only the separately configured implicit web annotation name is still reported
		editor.assertProblems("\"id\"|Unnecessary path variable definition");
	}

}
