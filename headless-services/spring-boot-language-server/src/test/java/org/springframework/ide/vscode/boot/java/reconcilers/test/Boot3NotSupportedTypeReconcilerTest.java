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
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.Boot3NotSupportedTypeReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class Boot3NotSupportedTypeReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "boot3notsupportedtypes";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-indexing";
	}
	
	@Override
	protected JdtAstReconciler getReconciler() {
		return new Boot3NotSupportedTypeReconciler();
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
	void imports() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.web.multipart.commons.CommonsMultipartResolver;
				
				class A {
				
					CommonsMultipartResolver a;
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot3JavaProblemType.JAVA_TYPE_NOT_SUPPORTED, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("org.springframework.web.multipart.commons.CommonsMultipartResolver", markedStr);

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void fqType() throws Exception {
		String source = """
				package example.demo;
				
				class A {
				
					org.springframework.web.multipart.commons.CommonsMultipartResolver a;
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot3JavaProblemType.JAVA_TYPE_NOT_SUPPORTED, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("org.springframework.web.multipart.commons.CommonsMultipartResolver", markedStr);

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void importWithWildcard() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.web.multipart.commons.*;
				
				class A {
				
					CommonsMultipartResolver a;
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot3JavaProblemType.JAVA_TYPE_NOT_SUPPORTED, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("CommonsMultipartResolver", markedStr);

		assertEquals(0, problem.getQuickfixes().size());
		
	}

	@Test
	void importWithWildcardNoProblem() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.web.multipart.*;
				
				class A {
				
					CommonsMultipartResolver a;
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}
}
