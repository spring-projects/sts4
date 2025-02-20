/*******************************************************************************
 * Copyright (c) 2023, 2025 VMware, Inc.
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

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.java.SpringAotJavaProblemType;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.NotRegisteredBeansReconciler;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

public class NotRegisteredBeansReconcilerTest extends BaseReconcilerTest {

	@Override
	protected String getFolder() {
		return "notregisteredbeanaot";
	}

	@Override
	protected String getProjectName() {
		return "test-spring-validations";
	}
	
	@Override
	protected JdtAstReconciler getReconciler() {
		return null;
	}
	
	private NotRegisteredBeansReconciler createReconciler(SpringMetamodelIndex springIndex) {
		return new NotRegisteredBeansReconciler(new QuickfixRegistry(), springIndex);
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
	void sanityTestAotProcessorIsRegisteredAsBean() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor {
				
					public A(String k) {}
				}
				""";
		
		SpringMetamodelIndex springIndex = new SpringMetamodelIndex();
		Bean aotBean = new Bean("a", "example.demo.A", new Location("docURI", new Range()), null, null, null, true, "symbolLabel");
		springIndex.updateBeans(getProjectName(), new Bean[] {aotBean});

		List<ReconcileProblem> problems = reconcile(() -> createReconciler(springIndex), "A.java", source, true);
		assertEquals(0, problems.size());
	}

	@Test
	void sanityTestAotProcessorIsNotRegisteredAsBean() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor {
				
					public A(String k) {}
				}
				""";
		
		SpringMetamodelIndex emptySpringIndex = new SpringMetamodelIndex();
		List<ReconcileProblem> problems = reconcile(() -> createReconciler(emptySpringIndex), "A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(0, problem.getQuickfixes().size());
		
	}
	
	@Test
	void sanityTestAotProcessorIsNotRegisteredAsBeanWithQuickFixes() throws Exception {
		String source = """
				package example.demo;
				
				import org.springframework.beans.factory.aot.BeanRegistrationAotProcessor;
				
				class A implements BeanRegistrationAotProcessor {
				
					public A(String k) {}
				}
				""";
		
		SpringMetamodelIndex springIndex = new SpringMetamodelIndex();
		Bean configBean = new Bean("testConfig", "example.demo.TestConfig", new Location("docURI", new Range()), null, null, null, true, "symbolLabel");
		springIndex.updateBeans(getProjectName(), new Bean[] {configBean});

		List<ReconcileProblem> problems = reconcile(() -> createReconciler(springIndex), "A.java", source, true);
		
		assertEquals(1, problems.size());
		
		ReconcileProblem problem = problems.get(0);
		
		assertEquals(SpringAotJavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT, problem.getType());
		
		String markedStr = source.substring(problem.getOffset(), problem.getOffset() + problem.getLength());
		assertEquals("A", markedStr);

		assertEquals(1, problem.getQuickfixes().size());
	}

}
