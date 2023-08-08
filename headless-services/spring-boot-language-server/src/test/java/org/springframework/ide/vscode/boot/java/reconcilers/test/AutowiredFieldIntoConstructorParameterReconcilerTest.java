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
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.AutowiredFieldIntoConstructorParameterReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class AutowiredFieldIntoConstructorParameterReconcilerTest extends BaseReconcilerTest {
	
	@Override
	protected String getFolder() {
		return "autowiredfieldtest";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-indexing";
	}
	
	protected JdtAstReconciler getReconciler() {
		return new AutowiredFieldIntoConstructorParameterReconciler(new QuickfixRegistry());
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
	void noConstructors() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_CONSTRUCTOR_PARAMETER_INJECTION, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@Autowired\n\tString a;", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
		
	}

	@Test
	void singleSimpleConstructorsNoMethodBodies() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
					A() {
					}
					
				}
				""";
		try {
			reconcile("A.java", source, false);
			fail("Should require complete AST. Exception must be thrown."); 
		} catch (RequiredCompleteAstException e) {
		}
				
	}

	@Test
	void singleSimpleConstructorsWithMethodBodies() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
					A() {
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_CONSTRUCTOR_PARAMETER_INJECTION, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@Autowired\n\tString a;", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
		
	}
	
	@Test
	void constrctorAssignsField() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
					A() {
						this.a = "qq"
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		assertEquals(0, problems.size());
		
	}

	@Test
	void autowiredConstructor() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
					A(int y) {
					}
					
					@Autowired
					A() {
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_CONSTRUCTOR_PARAMETER_INJECTION, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@Autowired\n\tString a;", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
		
	}

	@Test
	void autowiredConstructorAssigningField() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.annotation.Autowired;
				
				class A {
				
					@Autowired
					String a;
					
					A(int y) {
					}
					
					@Autowired
					A() {
						this.a = "qq"
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		assertEquals(0, problems.size());
		
	}

}
