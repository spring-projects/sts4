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
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.boot.java.reconcilers.ServerHttpSecurityLambdaDslReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class ServerHttpSecurityLambdaDslReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "serverhttpsecuritydsl";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-indexing";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new ServerHttpSecurityLambdaDslReconciler(new QuickfixRegistry());
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
	void requireFullAst() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.web.server.ServerHttpSecurity;
				
				class A {
				
					void something(ServerHttpSecurity security) {
					};
					
				}
				""";
		try {
			reconcile("A.java", source, false);
			fail("Should require full AST with method bodies");
		} catch (RequiredCompleteAstException e) {
			// pass
		}
		
	}

	@Test
	void sanityTest() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.web.server.ServerHttpSecurity;
				
				class A {
				
					void something(ServerHttpSecurity http) {
						http.authorizeExchange().pathMatchers("/blog/**").permitAll().anyExchange().authenticated();
					};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.JAVA_LAMBDA_DSL, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("http.authorizeExchange().pathMatchers(\"/blog/**\").permitAll().anyExchange().authenticated()", markedStr);

		assertEquals(3, problem.getQuickfixes().size());
		
	}
	
	@Test
	void noProblem() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.web.server.ServerHttpSecurity;
				
				class A {
				
						void something(ServerHttpSecurity http) {
							http.authorizeExchange(exchange -> exchange.pathMatchers("/blog/**").permitAll().anyExchange().authenticated());
						};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(0, problems.size());
		
	}

}
