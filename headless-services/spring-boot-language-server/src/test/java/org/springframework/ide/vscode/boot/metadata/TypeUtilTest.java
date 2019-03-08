/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.metadata.types.TypeParser;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.metadata.types.TypedProperty;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil.BeanPropertyNameMode;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil.EnumCaseMode;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.project.harness.ProjectsHarness;

/**
 * Tests for TypeUtil
 *
 * @author Kris De Volder
 * @author Alex Boyko
 *
 */
public class TypeUtilTest {

	private ProjectsHarness projects = ProjectsHarness.INSTANCE;
	private IJavaProject project;
	private TypeUtil typeUtil;

	private Type getPropertyType(Type type, String propName, EnumCaseMode enumMode, BeanPropertyNameMode beanMode) {
		List<TypedProperty> props = getProperties(type, enumMode, beanMode);
		assertNotNull(props);
		for (TypedProperty prop : props) {
			if (prop.getName().equals(propName)) {
				return prop.getType();
			}
		}
		return null;
	}

	private List<TypedProperty> getProperties(Type type, EnumCaseMode enumMode, BeanPropertyNameMode beanMode) {
		return typeUtil.getProperties(type, enumMode, beanMode);
	}

	@Test
	public void testGetTrickyProperties() throws Exception {
		useProject("tricky-getters-boot-1.3.1-app");
		List<TypedProperty> props = getProperties(TypeParser.parse("demo.TrickyGetters"), EnumCaseMode.LOWER_CASE, BeanPropertyNameMode.HYPHENATED);
		assertNotNull(props);
		List<String> names = props.stream().map(p -> p.getName()).collect(Collectors.toList());
		assertEquals(1, names.size());
		assertEquals("public-property", names.get(0));
	}

	@Test
	public void testGetProperties() throws Exception {
		useProject("enums-boot-1.3.2-app");
		assertNotNull(project.getIndex().findType("demo.Color"));
		assertNotNull(project.getIndex().findType("demo.ColorData"));


		Type data = TypeParser.parse("demo.ColorData");

		assertType("java.lang.Double",
				getPropertyType(data, "wavelen"));
		assertType("java.lang.String",
				getPropertyType(data, "name"));
		assertType("demo.Color",
				getPropertyType(data, "next"));
		assertType("demo.ColorData",
				getPropertyType(data, "nested"));
		assertType("java.util.List<java.lang.String>",
				getPropertyType(data, "tags"));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mapped-children"));
		assertType("java.util.Map<demo.Color,demo.ColorData>",
				getPropertyType(data, "color-children"));

		//Also gets aliased as camelCased names?
		assertType("java.util.Map<demo.Color,demo.ColorData>",
				getPropertyType(data, "colorChildren"));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mappedChildren"));

		//Gets aliased names only if asked for it?
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mappedChildren", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.CAMEL_CASE));
		assertType(null,
				getPropertyType(data, "mappedChildren", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.HYPHENATED));
		assertType(null,
				getPropertyType(data, "mapped-children", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.CAMEL_CASE));
		assertType("java.util.Map<java.lang.String,demo.ColorData>",
				getPropertyType(data, "mapped-children", EnumCaseMode.ORIGNAL, BeanPropertyNameMode.HYPHENATED));

	}

	@Test
	public void testGetEnumKeyedProperties() throws Exception {
		useProject("enums-boot-1.3.2-app");
		Type data = TypeParser.parse("java.util.Map<demo.Color,Something>");
		assertType("Something", getPropertyType(data, "red"));
		assertType("Something", getPropertyType(data, "green"));
		assertType("Something", getPropertyType(data, "blue"));
		assertType("Something", getPropertyType(data, "RED"));
		assertType("Something", getPropertyType(data, "GREEN"));
		assertType("Something", getPropertyType(data, "BLUE"));
		assertNull(getPropertyType(data, "not-a-color"));
	}

	private Type getPropertyType(Type type, String propName) {
		return getPropertyType(type, propName, EnumCaseMode.ALIASED, BeanPropertyNameMode.ALIASED);
	}

	private void assertType(String expectedType, Type actualType) {
		assertEquals(TypeParser.parse(expectedType), actualType);
	}

//	@Test
//	public void testTypeFromSignature() throws Exception {
//		useProject("enums-boot-1.3.2-app");
//		assertType("java.lang.String", Type.fromSignature("QString;", project.findType("demo.ColorData")));
//		assertType("java.lang.String", Type.fromSignature("Ljava.lang.String;", project.findType("demo.ColorData")));
//		assertType("java.lang.String[]", Type.fromSignature("[Ljava.lang.String;", project.findType("demo.ColorData")));
//		assertType("java.lang.String[]", Type.fromSignature("[QString;", project.findType("demo.ColorData")));
//	}

	private void useProject(String name) throws Exception {
		project = projects.mavenProject(name);;
		typeUtil = new TypeUtil(null, project);
	}

}
