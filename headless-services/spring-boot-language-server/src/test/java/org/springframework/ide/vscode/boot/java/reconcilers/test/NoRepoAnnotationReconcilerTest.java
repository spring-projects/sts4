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
import org.springframework.ide.vscode.boot.java.reconcilers.NoRepoAnnotationReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class NoRepoAnnotationReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "norepoannotation";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new NoRepoAnnotationReconciler(new QuickfixRegistry());
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
				
				import org.springframework.data.repository.Repository;
				
				@org.springframework.stereotype.Repository
				interface A extends Repository {
									
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@org.springframework.stereotype.Repository", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}
	
	@Test
	void inverseSanityTest() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.stereotype.Repository;
				
				@Repository
				interface A extends org.springframework.data.repository.Repository {
									
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@Repository", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}
	
	@Test
	void emptyRepoAnnotation() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.data.repository.Repository;
				
				@org.springframework.stereotype.Repository()
				interface A extends Repository {
									
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_REPOSITORY, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("@org.springframework.stereotype.Repository()", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}

}
