/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.metadata;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.metadata.types.TypeParser;

/**
 * Type parser tests
 *
 * @author Kris De Volder
 */
public class TypeParserTest {

	@Test
	public void testNonGeneric() throws Exception {
		Type type = TypeParser.parse("java.lang.String");
		assertEquals("java.lang.String", type.getErasure());
		assertFalse(type.isGeneric());
		assertEquals("java.lang.String", type.toString());
	}

	@Test
	public void testSimpleGeneric() throws Exception {
		Type type = TypeParser.parse("List<Foo>");
		assertEquals("List", type.getErasure());
		assertTrue(type.isGeneric());

		Type[] params = type.getParams();
		assertEquals(1, params.length);
		type = params[0];
		assertEquals("Foo", type.getErasure());
		assertFalse(type.isGeneric());
	}

	@Test
	public void testMultipleParams() throws Exception {
		Type type = TypeParser.parse("Map<Foo,Bar>");
		assertEquals("Map", type.getErasure());
		assertTrue(type.isGeneric());

		Type[] params = type.getParams();
		assertEquals(2, params.length);

		type = params[0];
		assertEquals("Foo", type.getErasure());
		assertFalse(type.isGeneric());

		type = params[1];
		assertEquals("Bar", type.getErasure());
		assertFalse(type.isGeneric());
	}

	@Test
	public void testNestedGenerics() throws Exception {
		Type type = TypeParser.parse("Map<Foo,List<Bar>>");
		assertEquals("Map", type.getErasure());
		assertTrue(type.isGeneric());

		assertEquals("Map<Foo,List<Bar>>", type.toString());
	}

	@Test
	public void testTypeEquality() throws Exception {
		Type type1 = TypeParser.parse("Map<Foo,List<Bar>>");
		Type type2 = TypeParser.parse("Map<Foo,List<Bar>>");
		Type type3 = TypeParser.parse("Map<Bar,List<Bar>>");
		assertEquals(type1, type2);
		assertNotEquals(type1, type3);
	}
	
}
