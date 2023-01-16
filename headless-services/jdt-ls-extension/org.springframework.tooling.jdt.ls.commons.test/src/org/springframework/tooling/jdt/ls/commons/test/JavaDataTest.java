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
package org.springframework.tooling.jdt.ls.commons.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.ide.vscode.commons.protocol.java.TypeData;
import org.springframework.ide.vscode.commons.protocol.java.TypeData.FieldData;
import org.springframework.tooling.jdt.ls.commons.Logger;
import org.springframework.tooling.jdt.ls.commons.java.JavaData;

public class JavaDataTest {
	
	private static final long LABEL_FLAGS=
			JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.M_PRE_RETURNTYPE
			| JavaElementLabels.M_PARAMETER_ANNOTATIONS
			| JavaElementLabels.M_PARAMETER_TYPES
			| JavaElementLabels.M_PARAMETER_NAMES
			| JavaElementLabels.M_EXCEPTIONS
			| JavaElementLabels.F_PRE_TYPE_SIGNATURE
			| JavaElementLabels.M_PRE_TYPE_PARAMETERS
			| JavaElementLabels.T_TYPE_PARAMETERS
			| JavaElementLabels.USE_RESOLVED;

	private static final long LOCAL_VARIABLE_FLAGS= LABEL_FLAGS & ~JavaElementLabels.F_FULLY_QUALIFIED | JavaElementLabels.F_POST_QUALIFIED;

	private static final long COMMON_SIGNATURE_FLAGS = LABEL_FLAGS & ~JavaElementLabels.ALL_FULLY_QUALIFIED
			| JavaElementLabels.T_FULLY_QUALIFIED | JavaElementLabels.M_FULLY_QUALIFIED;
	
	private static String label(IJavaElement element) {
		try {
			if (element instanceof ILocalVariable) {
				return JavaElementLabels.getElementLabel(element,LOCAL_VARIABLE_FLAGS);
			} else {
				return JavaElementLabels.getElementLabel(element,COMMON_SIGNATURE_FLAGS);
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Rule public TemporaryFolder tmp = new TemporaryFolder();
	
	private JavaData javaData = new JavaData(JavaDataTest::label, Logger.DEFAULT);
	
	@After
	public void tearDown() throws Exception {
		TestUtils.deleteAllProjects();
	}

	
	@Test public void simplePojoData() throws Exception {
		IProject project = TestUtils.createTestProject("java-data", tmp);
		TypeData d = javaData.typeData(project.getLocationURI().toASCIIString(), "Lcom/java/data/SimplePojo;", false);
		assertNotNull(d);
		assertEquals(3, d.getFields().size());
		assertEquals(6, d.getMethods().size());

		FieldData f = d.getFields().get(0);
		assertEquals("id", f.getName());
		assertEquals("Ljava/lang/String;", f.getType().getName());

		f = d.getFields().get(1);
		assertEquals("count", f.getName());
		assertEquals("I", f.getType().getName());

		f = d.getFields().get(2);
		assertEquals("items", f.getName());
		assertEquals("Ljava/util/List<Ljava/lang/String;>;", f.getType().getName());
	}
	
	@Test public void recordData() throws Exception {
		IProject project = TestUtils.createTestProject("java-data", tmp);
		TypeData d = javaData.typeData(project.getLocationURI().toASCIIString(), "Lcom/java/data/UserPropertiesRecord;", false);
		assertNotNull(d);
		assertEquals(3, d.getFields().size());
		assertEquals(0, d.getMethods().size());
		
		FieldData f = d.getFields().get(0);
		assertEquals("name", f.getName());
		assertEquals("Ljava/lang/String;", f.getType().getName());

		f = d.getFields().get(1);
		assertEquals("password", f.getName());
		assertEquals("Ljava/lang/String;", f.getType().getName());

		f = d.getFields().get(2);
		assertEquals("roles", f.getName());
		assertEquals("Ljava/util/List<Ljava/lang/String;>;", f.getType().getName());
	}
}
