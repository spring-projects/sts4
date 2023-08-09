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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NoRequestMappingAnnotationReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class NoRequestMappingAnnotationReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "norequestmapping";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new NoRequestMappingAnnotationReconciler(new QuickfixRegistry());
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
	void sanityTest() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.web.bind.annotation.RequestMapping;
				
				
				@RequestMapping("/hello")
				class A {
				
					@RequestMapping("/1")
					String hello1() {
						return "1";
					};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_PRECISE_REQUEST_MAPPING, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@RequestMapping(\"/1\")", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}

	@Test
	void noProblems() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.web.bind.annotation.RequestMapping;
				import org.springframework.context.annotation.Bean;
				import org.springframework.web.bind.annotation.GetMapping;
				
				
				@RequestMapping("/hello")
				class A {
				
					@Bean
					Integer someBean() {
						return 0;
					}
				
					@GetMapping("/1")
					String hello1() {
						return "1";
					};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}
}
