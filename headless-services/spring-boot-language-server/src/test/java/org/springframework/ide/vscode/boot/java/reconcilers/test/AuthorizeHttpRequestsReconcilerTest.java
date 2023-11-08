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
import org.springframework.ide.vscode.boot.java.reconcilers.AuthorizeHttpRequestsReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class AuthorizeHttpRequestsReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "authorizerequests";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-indexing";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new AuthorizeHttpRequestsReconciler(new QuickfixRegistry());
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
	void requireFullAst_1() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.builders.HttpSecurity;
				
				class A {
				
					void something(HttpSecurity security) {
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
	void requireFullAst_2() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.configurers.AbstractInterceptUrlConfigurer;
				
				class A {
				
					void something() {
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
	void requireFullAst_3() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
				
				class A {
				
					void something(ExpressionUrlAuthorizationConfigurer.ExpressionInterceptUrlRegistry reg) {
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
				
				import org.springframework.security.config.annotation.web.builders.HttpSecurity;
				
				class A {
				
					void something(HttpSecurity security) {
						security.authorizeRequests().mvcMatchers();
					};
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.HTTP_SECURITY_AUTHORIZE_HTTP_REQUESTS, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("authorizeRequests", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}

	@Test
	void multipleProblems() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.context.annotation.Bean;
				import org.springframework.context.annotation.Configuration;
				import org.springframework.security.config.annotation.web.builders.HttpSecurity;
				import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
				import org.springframework.security.web.SecurityFilterChain;

				@Configuration
				public class JdbcSecurityConfiguration {
					@Bean
					SecurityFilterChain web(HttpSecurity http) throws Exception {
						ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry reqs = http.authorizeRequests();
						reqs.antMatchers("/ll").authenticated();
						return http.build();
					}
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.HTTP_SECURITY_AUTHORIZE_HTTP_REQUESTS, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("authorizeRequests", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}
}
