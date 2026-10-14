/*******************************************************************************
 * Copyright (c) 2026 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.jdt.refactoring;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.index.cache.IndexGsonTypeFactories;
import org.springframework.ide.vscode.boot.java.jdt.refactoring.RemoveModelAttributeNameRefactoring.Fix;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Unit tests for {@link RemoveModelAttributeNameRefactoring}.
 *
 * @author Nihal Kumar Sharma
 */
class RemoveModelAttributeNameRefactoringTest {

	private static Map<String, String> defaultFormatterOptions() {
		Map<String, String> options = JavaCore.getOptions();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		return options;
	}

	private static CompilationUnit parseSource(String source, String... classpath) {
		ASTParser parser = ASTParser.newParser(AST.JLS25);
		parser.setSource(source.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		parser.setEnvironment(classpath, new String[0], null, true);
		parser.setUnitName("Test.java");
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_21, options);
		parser.setCompilerOptions(options);
		return (CompilationUnit) parser.createAST(null);
	}

	/**
	 * The refactoring only removes names it can confirm as redundant through resolved bindings,
	 * so {@code @ModelAttribute} has to be on the parser's classpath.
	 */
	private static String springWebJar() throws Exception {
		return new File(ModelAttribute.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
	}

	private static String applyRefactoring(String source, JdtRefactoring refactoring) throws Exception {
		return applyRefactoring(parseSource(source, springWebJar()), source, refactoring);
	}

	private static String applyRefactoring(CompilationUnit cu, String source, JdtRefactoring refactoring) throws Exception {
		ASTRewrite rewrite = ASTRewrite.create(cu.getAST());
		refactoring.apply(rewrite, cu);
		Document doc = new Document(source);
		TextEdit edit = rewrite.rewriteAST(doc, defaultFormatterOptions());
		edit.apply(doc);
		return doc.get();
	}

	private static String removeName(String source, String annotation, String name) throws Exception {
		return applyRefactoring(source, new RemoveModelAttributeNameRefactoring(new Fix(source.indexOf(annotation), name)));
	}

	private static String controller(String parameters) {
		return """
				package com.example;

				import org.springframework.validation.BindingResult;
				import org.springframework.web.bind.annotation.ModelAttribute;

				class UserController {

					public String save(%s) {
						return "user";
					}
				}

				class User {}
				class Account {}
				class Order {}
				""".formatted(parameters);
	}

	@Test
	void singleMemberNameIsRemoved() throws Exception {
		String source = controller("@ModelAttribute(\"user\") User account, BindingResult result");

		assertEquals(controller("@ModelAttribute User account, BindingResult result"),
				removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void valueAttributeIsRemoved() throws Exception {
		String source = controller("@ModelAttribute(value = \"user\") User user");

		assertEquals(controller("@ModelAttribute User user"), removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void nameAttributeIsRemovedAndOtherAttributesAreKept() throws Exception {
		String source = controller("@ModelAttribute(name = \"user\", binding = false) User user");

		assertEquals(controller("@ModelAttribute(binding = false) User user"), removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void trailingValueAttributeIsRemoved() throws Exception {
		String source = controller("@ModelAttribute(binding = false, value = \"user\") User user");

		assertEquals(controller("@ModelAttribute(binding = false) User user"), removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void fullyQualifiedAnnotationIsKept() throws Exception {
		String source = controller("@org.springframework.web.bind.annotation.ModelAttribute(\"user\") User user");

		assertEquals(controller("@org.springframework.web.bind.annotation.ModelAttribute User user"),
				removeName(source, "@org.springframework.web.bind.annotation.ModelAttribute", "user"));
	}

	@Test
	void otherAnnotationsAndCommentsAreKept() throws Exception {
		String source = controller("@Valid /* form */ @ModelAttribute(\"user\") User user");

		assertEquals(controller("@Valid /* form */ @ModelAttribute User user"), removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void allNamesInFileAreRemoved() throws Exception {
		String source = controller("@ModelAttribute(\"user\") User user, @ModelAttribute(name = \"order\", binding = false) Order order");

		RemoveModelAttributeNameRefactoring refactoring = new RemoveModelAttributeNameRefactoring(
				new Fix(source.indexOf("@ModelAttribute(\"user\")"), "user"),
				new Fix(source.indexOf("@ModelAttribute(name"), "order"));

		assertEquals(controller("@ModelAttribute User user, @ModelAttribute(binding = false) Order order"),
				applyRefactoring(source, refactoring));
	}

	@Test
	void namesThatChangedSinceReconcilingAreKept() throws Exception {
		String source = controller("@ModelAttribute(\"account\") User user, @RequestParam(\"user\") String name, @ModelAttribute(Names.USER) User other");

		RemoveModelAttributeNameRefactoring refactoring = new RemoveModelAttributeNameRefactoring(
				new Fix(source.indexOf("@ModelAttribute(\"account\")"), "user"),
				new Fix(source.indexOf("@RequestParam"), "user"),
				new Fix(source.indexOf("@ModelAttribute(Names.USER)"), "user"));

		assertEquals(source, applyRefactoring(source, refactoring));
	}

	@Test
	void nameIsKeptWhenParameterTypeChangedSinceReconciling() throws Exception {
		// the fix was computed for "@ModelAttribute("user") User customer", after which the type changed
		String source = controller("@ModelAttribute(\"user\") Account customer");

		assertEquals(source, removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void unrelatedAnnotationWithSameSimpleNameIsKept() throws Exception {
		String source = """
				package com.example;

				class UserController {

					public String save(@ModelAttribute("user") User user) {
						return "user";
					}
				}

				@interface ModelAttribute {
					String value();
				}

				class User {}
				""";

		assertEquals(source, removeName(source, "@ModelAttribute", "user"));
	}

	@Test
	void nameIsKeptWithoutResolvedBindings() throws Exception {
		String source = controller("@ModelAttribute(\"user\") User user");
		RemoveModelAttributeNameRefactoring refactoring = new RemoveModelAttributeNameRefactoring(new Fix(source.indexOf("@ModelAttribute"), "user"));

		// without spring-web on the classpath, the annotation can't be resolved
		assertEquals(source, applyRefactoring(parseSource(source), source, refactoring));
	}

	@Test
	void refactoringSurvivesQuickfixSerialization() throws Exception {
		String source = controller("@ModelAttribute(name = \"user\", binding = false) User user");
		JdtFixDescriptor descriptor = new JdtFixDescriptor(
				new RemoveModelAttributeNameRefactoring(new Fix(source.indexOf("@ModelAttribute"), "user")),
				List.of("file:///UserController.java"), "Remove implicit `@ModelAttribute` name");

		Gson gson = new GsonBuilder().registerTypeAdapterFactory(IndexGsonTypeFactories.jdtRefactorings()).create();
		JdtFixDescriptor deserialized = gson.fromJson(gson.toJsonTree(descriptor), JdtFixDescriptor.class);

		assertEquals(controller("@ModelAttribute(binding = false) User user"), applyRefactoring(source, deserialized.refactoring()));
	}

}
