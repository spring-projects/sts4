/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.java.properties.parser.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.Parser;
import org.springframework.ide.vscode.java.properties.parser.Problem;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Comment;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;

public class PropertiesAntlrParserTest {
	
	Parser parser = new AntlrParser();
	
	private void testCommentLine(String text, String expectedComment) {
		ParseResults results = parser.parse(text);
		assertTrue(results.syntaxErrors.isEmpty());
		assertTrue(results.problems.isEmpty());
		assertEquals(1, results.ast.getAllNodes().size());
		
		List<Comment> commentLines = results.ast.getNodes(Comment.class);
		assertEquals(1, commentLines.size());
		
		Comment comment = commentLines.get(0);
		assertEquals(expectedComment, text.substring(comment.getOffset(), comment.getOffset() + comment.getLength()));
	}
	
	private void testPropertyLine(String text, 
			String expectedKey, String expectedEncodedKey,
			String expectedValue, String expectedEncodedValue) {
		ParseResults results = parser.parse(text);
		if (!results.syntaxErrors.isEmpty()) {
			fail(results.syntaxErrors.get(0).getMessage());
		}
		assertTrue(results.problems.isEmpty());
		assertEquals(1, results.ast.getAllNodes().size());
		
		List<KeyValuePair> propertyLines = results.ast.getNodes(KeyValuePair.class);
		assertEquals(1, propertyLines.size());
		
		KeyValuePair line = propertyLines.get(0);
		
		assertNotNull(line.getKey());
		assertNotNull(line.getValue());
		
		assertEquals(expectedKey, line.getKey().decode());
		assertEquals(expectedValue, line.getValue().decode());
		assertEquals(expectedEncodedKey, text.substring(line.getKey().getOffset(), line.getKey().getOffset() + line.getKey().getLength()));
		assertEquals(expectedEncodedValue, text.substring(line.getValue().getOffset(), line.getValue().getOffset() + line.getValue().getLength()));
	}
	
	@Test
	public void testExclamationComment() throws Exception {
		testCommentLine("! This is comment", "! This is comment");
	}
	
	@Test
	public void testExclamationCommentWithSpaces() throws Exception {
		testCommentLine("    ! This is comment =  ", "! This is comment =  ");
	}
	
	@Test
	public void testSharpComment() throws Exception {
		testCommentLine("# This is comment", "# This is comment");
	}
	
	@Test
	public void testSharpCommentWithSpaces() throws Exception {
		testCommentLine("    # This is comment =  ", "# This is comment =  ");
	}

	@Test
	public void testPropertyWithEqualsSeparator() throws Exception {
		testPropertyLine("key=value", "key", "key", "value", "value");
	}
	
	@Test
	public void testPropertyWithEqualsSeparatorAndSpaces() throws Exception {
		testPropertyLine("key \t = \t \tvalue", "key", "key", "value", " \t \tvalue");
	}

	@Test
	public void testPropertyWithColonSeparator() throws Exception {
		testPropertyLine("key:value", "key", "key", "value", "value");
	}

	@Test
	public void testPropertyWithColonSeparatorAndSpaces() throws Exception {
		testPropertyLine("key \t : \t \tvalue", "key", "key", "value", " \t \tvalue");
	}
	
	@Test
	public void testPropertyWithEscapedValue() {
		testPropertyLine("key=something \\nescapy", "key", "key", "something \nescapy", "something \\nescapy");
		testPropertyLine("key=something \\\\escapy", "key", "key", "something \\escapy", "something \\\\escapy");
		testPropertyLine("key=something\\:escapy", "key", "key", "something:escapy", "something\\:escapy");
		testPropertyLine("key=something\\=escapy", "key", "key", "something=escapy", "something\\=escapy");
		testPropertyLine("key=something\\ escapy", "key", "key", "something escapy", "something\\ escapy");
		testPropertyLine("key=something\\'escapy", "key", "key", "something'escapy", "something\\'escapy");
		testPropertyLine("key=something\\#escapy", "key", "key", "something#escapy", "something\\#escapy");
		testPropertyLine("key=something\\!escapy", "key", "key", "something!escapy", "something\\!escapy");
	}

	@Test
	public void testPropertyWithSpaceSeparator() throws Exception {
		testPropertyLine("key value", "key", "key", "value", "value");
	}
	
	@Test
	public void testSpacesSeparation() throws Exception {
        testPropertyLine("key       \t    value", "key", "key", "value", "value");
	}
	
	@Test
	public void testValueWithSpaces() throws Exception {
		testPropertyLine("key=value 1 and more staff \t that is all", "key", "key", "value 1 and more staff \t that is all", "value 1 and more staff \t that is all");
	}

	@Test
	public void testKeyWithLeadingSpaces() throws Exception {
        testPropertyLine("        key2:value 2", "key2", "key2", "value 2", "value 2");
	}
	
	@Test
	public void testKeyWithLeadingAndTrailingSpaces() throws Exception {
        testPropertyLine(" key3     \t     :value3", "key3", "key3", "value3", "value3");
	}

	@Test
	public void testEncodedKeyAndValue() throws Exception {
        testPropertyLine("ke\\:\\=y4=v\\\na\\\nl\\\nu\\\ne  \t  4", "ke:=y4", "ke\\:\\=y4", "value  \t  4", "v\\\na\\\nl\\\nu\\\ne  \t  4");
	}

	@Test
	public void testEqualsValue() throws Exception {
        testPropertyLine("key\\=5==", "key=5", "key\\=5", "=", "=");
	}

	@Test
	public void testEqualsValueSeparatedWithEqualsAndSpace() throws Exception {
        testPropertyLine("key7 = =", "key7", "key7", "=", " =");
	}
	
	@Test
	public void testValueWithTrailingSpaces() throws Exception {
		testPropertyLine("key = value  1  ", "key", "key", "value  1  ", " value  1  ");
	}
	
	@Test
	public void testUnodeCharKeyAndValue() throws Exception {
		testPropertyLine("k\u2b22ey\u2b28 = val\u2b24ue  1\u2b24  ", "k\u2b22ey\u2b28", "k\u2b22ey\u2b28", "val\u2b24ue  1\u2b24  ", " val\u2b24ue  1\u2b24  ");
	}
	
	@Test
	public void testVariousCharsInValue() throws Exception {
		for (char c = '!'; c < '@'; c++) {
			testPropertyLine("key=va" + c + "lue", "key", "key", "va" + c + "lue", "va" + c + "lue");
		}
	}
	
	@Test
	public void testSyntaxError() throws Exception {
		String text = "abrakadabra";
		ParseResults results = parser.parse(text);
		assertEquals(1, results.syntaxErrors.size());
		assertTrue(results.problems.isEmpty());
		// One property line recorded. With key and empty value 
		assertEquals(1, results.ast.getAllNodes().size());
		
		Problem syntaxError = results.syntaxErrors.get(0);
		assertEquals(0, syntaxError.getOffset());
		assertEquals(text.length(), syntaxError.getLength());
	}
	
	@Test
	public void testMultipleSyntaxErrors() throws Exception {
		String text = "abrakadabra\nkey:value\nsdcsdc";
		ParseResults results = parser.parse(text);
		assertEquals(2, results.syntaxErrors.size());
		assertTrue(results.problems.isEmpty());
		// One property line recorded. With key and empty value 
		assertEquals(3, results.ast.getAllNodes().size());
		List<KeyValuePair> lines = results.ast.getNodes(KeyValuePair.class);
		assertEquals(3, lines.size());
		
		// Test valid part
		KeyValuePair validLine = lines.get(1);
		assertNotNull(validLine.getKey());
		assertNotNull(validLine.getValue());
		assertEquals("key", text.substring(validLine.getKey().getOffset(), validLine.getKey().getOffset() + validLine.getKey().getLength()));
		assertEquals("value", text.substring(validLine.getValue().getOffset(), validLine.getValue().getOffset() + validLine.getValue().getLength()));
		
		// Test errors
		Problem syntaxError1 = results.syntaxErrors.get(0);
		assertEquals(0, syntaxError1.getOffset());
		assertEquals(11, syntaxError1.getLength());
		Problem syntaxError2 = results.syntaxErrors.get(1);
		assertEquals(22, syntaxError2.getOffset());
		assertEquals(6, syntaxError2.getLength());
	}
	
}
