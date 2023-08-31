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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.beans.ConfigBeanSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.beans.FeignClientBeanSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.reconcilers.AddConfigurationIfBeansPresentReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;

public class AddConfigurationIfBeansPresentReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "addconfiguration";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@Override
	protected JdtAstReconciler getReconciler() {
		return new AddConfigurationIfBeansPresentReconciler(new QuickfixRegistry());
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
	void basicCase() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.context.annotation.Bean;
				
				class A {
				
					@Bean
					String myBean() {
						return "my-bean";
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile("A.java", source, false);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(Boot2JavaProblemType.MISSING_CONFIGURATION_ANNOTATION, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(2, problem.getQuickfixes().size());
		
	}

	@Test
	void beanCase() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.context.annotation.Bean;
				
				class A {
				
					@Bean
					String myBean() {
						return "my-bean";
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile(() -> {
			AddConfigurationIfBeansPresentReconciler r = new AddConfigurationIfBeansPresentReconciler(new QuickfixRegistry());
			
			WorkspaceSymbol workspaceSymbol = new WorkspaceSymbol("testConfig", SymbolKind.Class, Either.forLeft(new Location("file:///someUri", new Range())));
			ConfigBeanSymbolAddOnInformation beanSymbolAddOn = new ConfigBeanSymbolAddOnInformation("a", "example.demo.A");
			EnhancedSymbolInformation beanSymbol = new EnhancedSymbolInformation(workspaceSymbol, new SymbolAddOnInformation[] { beanSymbolAddOn });
			
			SpringSymbolIndex mockSymbolIndex = mock(SpringSymbolIndex.class);
			
			when(mockSymbolIndex.getEnhancedSymbols(any(IJavaProject.class))).thenReturn(List.of(beanSymbol));
			
			ApplicationContext context = mock(ApplicationContext.class);
			when(context.getBean(SpringSymbolIndex.class)).thenReturn(mockSymbolIndex);
			
			r.setApplicationContext(context);
			
			return r;
		}, "A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}

	@Test
	void feignClientConfigCase() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.context.annotation.Bean;
				
				class A {
				
					@Bean
					String myBean() {
						return "my-bean";
					}
					
				}
				""";
		List<ReconcileProblem> problems = reconcile(() -> {
			AddConfigurationIfBeansPresentReconciler r = new AddConfigurationIfBeansPresentReconciler(new QuickfixRegistry());
			
			WorkspaceSymbol workspaceSymbol = new WorkspaceSymbol("testConfig", SymbolKind.Class, Either.forLeft(new Location("file:///someUri", new Range())));
			FeignClientBeanSymbolAddOnInformation beanSymbolAddOn = new FeignClientBeanSymbolAddOnInformation("b", "example.demo.B", "example.demo.A");
			EnhancedSymbolInformation beanSymbol = new EnhancedSymbolInformation(workspaceSymbol, new SymbolAddOnInformation[] { beanSymbolAddOn });
			
			SpringSymbolIndex mockSymbolIndex = mock(SpringSymbolIndex.class);
			
			when(mockSymbolIndex.getEnhancedSymbols(any(IJavaProject.class))).thenReturn(List.of(beanSymbol));
			
			ApplicationContext context = mock(ApplicationContext.class);
			when(context.getBean(SpringSymbolIndex.class)).thenReturn(mockSymbolIndex);
			
			r.setApplicationContext(context);
			
			return r;
		}, "A.java", source, false);
		
		assertEquals(0, problems.size());
		
	}
}
