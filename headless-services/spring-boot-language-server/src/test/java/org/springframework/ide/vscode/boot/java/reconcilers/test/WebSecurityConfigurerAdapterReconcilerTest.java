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
import org.springframework.ide.vscode.boot.java.reconcilers.WebSecurityConfigurerAdapterReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class WebSecurityConfigurerAdapterReconcilerTest extends BaseReconcilerTest {
	
	@Override
	protected String getFolder() {
		return "websecurityconfigurer";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-indexing";
	}
	
	protected JdtAstReconciler getReconciler() {
		return new WebSecurityConfigurerAdapterReconciler(new QuickfixRegistry());
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
	void typeImport() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
				import org.springframework.context.annotation.Configuration;
				
				@Configuration
				class A extends WebSecurityConfigurerAdapter {
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_SECURITY_CONFIGURER_ADAPTER, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("WebSecurityConfigurerAdapter", markedStr);
		
		assertEquals(2, problem.getQuickfixes().size());

	}

	@Test
	void typeImportWithWildCard() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.configuration.*;
				import org.springframework.context.annotation.Configuration;
				
				@Configuration
				class A extends WebSecurityConfigurerAdapter {
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_SECURITY_CONFIGURER_ADAPTER, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("WebSecurityConfigurerAdapter", markedStr);		

		assertEquals(2, problem.getQuickfixes().size());
	}

	@Test
	void fqType() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.context.annotation.Configuration;
				
				@Configuration
				class A extends org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter {
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_SECURITY_CONFIGURER_ADAPTER, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter", markedStr);		

		assertEquals(2, problem.getQuickfixes().size());
	}
	
	@Test
	void notConfigBean() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
				
				class A extends WebSecurityConfigurerAdapter {
				}
				""";
		
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.WEB_SECURITY_CONFIGURER_ADAPTER, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("WebSecurityConfigurerAdapter", markedStr);		

		assertEquals(0, problem.getQuickfixes().size());
	}

}
