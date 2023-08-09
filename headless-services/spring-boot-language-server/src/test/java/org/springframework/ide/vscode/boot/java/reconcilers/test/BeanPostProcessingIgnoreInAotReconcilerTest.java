/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.BeanPostProcessingIgnoreInAotReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class BeanPostProcessingIgnoreInAotReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "beanpostprocessingaot";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new BeanPostProcessingIgnoreInAotReconciler(new QuickfixRegistry());
	}

	@BeforeEach
	void setup() throws Exception {
		super.setup();
	}
	
	@AfterEach
	void tearDown() throws Exception {
		super.tearDown();
	}
	
	@Test
	void noMethod() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.config.BeanPostProcessor;
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanPostProcessor, BeanRegistrationAotProcessor{
				
					A() {};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
		
	}
	
	@Test
	void withMethodReturningTrue_IncompleteAst() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.config.BeanPostProcessor;
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanPostProcessor, BeanRegistrationAotProcessor{
				
					A() {};
					
					public boolean isBeanExcludedFromAotProcessing() { return true; }
					
				}
				""";
		try {
			reconcile("A.java", source, false);
			fail("Should require complete AST");
		} catch (RequiredCompleteAstException e) {
			// good
		}
	}


	@Test
	void withMethodReturningTrue_CompleteAst() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.config.BeanPostProcessor;
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanPostProcessor, BeanRegistrationAotProcessor{
				
					A() {};
					
					public boolean isBeanExcludedFromAotProcessing() { return true; }
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_POST_PROCESSOR_IGNORED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
		
	}

	@Test
	void withMethodReturningFalse() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.config.BeanPostProcessor;
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanPostProcessor, BeanRegistrationAotProcessor{
				
					A() {};
					
					public boolean isBeanExcludedFromAotProcessing() { return false; }
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(0, problems.size());
		
	}
	
	@Test
	void noBeanPostProcessor() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor{
				
					A() {};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(0, problems.size());
		
	}

}
