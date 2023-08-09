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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.boot.java.beans.ConfigBeanSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NotRegisteredBeansReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;


public class NotRegisteredBeansReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "notregisteredbeanaot";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JdtAstReconciler getReconciler() {
		NotRegisteredBeansReconciler reconciler = new NotRegisteredBeansReconciler(new QuickfixRegistry());
		SpringSymbolIndex mockSymbolIndex = mock(SpringSymbolIndex.class);
		when(mockSymbolIndex.getSymbols(any(Predicate.class))).thenReturn(Stream.empty());
		
		ApplicationContext context = mock(ApplicationContext.class);
		when(context.getBean(SpringSymbolIndex.class)).thenReturn(mockSymbolIndex);
		
		reconciler.setApplicationContext(context);
		return reconciler;
	}
	
	@SuppressWarnings("unchecked")
	private NotRegisteredBeansReconciler createReconciler(EnhancedSymbolInformation... beanSymbols) {
		NotRegisteredBeansReconciler reconciler = new NotRegisteredBeansReconciler(new QuickfixRegistry());
		SpringSymbolIndex mockSymbolIndex = mock(SpringSymbolIndex.class);
		when(mockSymbolIndex.getSymbols(any(Predicate.class))).thenReturn(Stream.empty());
		when(mockSymbolIndex.getEnhancedSymbols(any(IJavaProject.class))).thenReturn(Arrays.asList(beanSymbols));
		
		ApplicationContext context = mock(ApplicationContext.class);
		when(context.getBean(SpringSymbolIndex.class)).thenReturn(mockSymbolIndex);
		
		reconciler.setApplicationContext(context);
		return reconciler;
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
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor {
				
					public A(String k) {}
				}
				""";
		List<ReconcileProblem> problems = reconcile(() -> createReconciler(), "A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void sanityTestWithQuickFixes() throws Exception {
		Path configClassPath = createFile("TestConfig.java", """
				package example.demo;
				
				import org.springframework.context.annotation.Configuration;
				
				@Configuration
				class TestConfig {
				}
		""");
		
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor {
				
					public A(String k) {}
				}
				""";
		
		WorkspaceSymbol workspaceSymbol = new WorkspaceSymbol("testConfig", SymbolKind.Class, Either.forLeft(new Location(configClassPath.toUri().toASCIIString(), new Range())));
		ConfigBeanSymbolAddOnInformation configBeanAddOn = new ConfigBeanSymbolAddOnInformation("testConfig", "example.demo.TestConfig");
		
		List<ReconcileProblem> problems = reconcile(() -> createReconciler(new EnhancedSymbolInformation(workspaceSymbol, new SymbolAddOnInformation[] { configBeanAddOn })), "A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
	}


}
