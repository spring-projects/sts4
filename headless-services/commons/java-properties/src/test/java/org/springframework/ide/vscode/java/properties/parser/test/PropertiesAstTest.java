/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.springframework.ide.vscode.java.properties.antlr.parser.AntlrParser;
import org.springframework.ide.vscode.java.properties.parser.ParseResults;
import org.springframework.ide.vscode.java.properties.parser.Parser;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Comment;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.EmptyLine;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Key;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.KeyValuePair;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;

public class PropertiesAstTest {
	
	Parser parser = new AntlrParser();
	
	@Test
	public void testLines1() throws Exception {
		ParseResults results = parser.parse("# Comment\n\n \t   \t \n\t\t\n");
		assertTrue(results.syntaxErrors.isEmpty());
		assertTrue(results.problems.isEmpty());
		assertEquals(4, results.ast.getAllNodes().size());
		assertEquals(1, results.ast.getNodes(Comment.class).size());
		assertEquals(3, results.ast.getNodes(EmptyLine.class).size());
	}
	
	@Test
	public void testLines2() throws Exception {
		ParseResults results = parser.parse("\n\n \t   \t \n# Comment\n\t\t\n");
		assertTrue(results.syntaxErrors.isEmpty());
		assertTrue(results.problems.isEmpty());
		assertEquals(5, results.ast.getAllNodes().size());
		assertEquals(1, results.ast.getNodes(Comment.class).size());
		assertEquals(4, results.ast.getNodes(EmptyLine.class).size());
	}
	
	@Test
	public void testLines3() throws Exception {
		ParseResults results = parser.parse("# Comment\n\nkey = value  1  \n \t   \t \n\t\t\n");
		assertTrue(results.syntaxErrors.isEmpty());
		assertTrue(results.problems.isEmpty());
		assertEquals(5, results.ast.getAllNodes().size());
		assertEquals(1, results.ast.getNodes(Comment.class).size());
		assertEquals(1, results.ast.getNodes(KeyValuePair.class).size());		
		assertEquals(3, results.ast.getNodes(EmptyLine.class).size());
	}
	
	@Test
	public void testLines4() throws Exception {
		ParseResults results = parser.parse("# Comment-1\n\nkey = value  1  \n# Comment-2");
		assertEquals(4, results.ast.getAllNodes().size());
		assertEquals(2, results.ast.getNodes(Comment.class).size());
		assertEquals(1, results.ast.getNodes(KeyValuePair.class).size());		
		assertEquals(1, results.ast.getNodes(EmptyLine.class).size());
	}
	
	@Test
	public void testLines5() throws Exception {
		ParseResults results = parser.parse("#comment\nliquibase.enabled=\n#comment");
		assertEquals(3, results.ast.getAllNodes().size());
		assertEquals(2, results.ast.getNodes(Comment.class).size());
		assertEquals(1, results.ast.getNodes(KeyValuePair.class).size());		
	}
		
	@Test
	public void positionComment() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value\n");
		
		Node node = results.ast.findNode(7);
		assertTrue(node instanceof Comment);
		assertTrue(node.getOffset() <= 7 && 7 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(9);
		assertTrue(node instanceof Comment);
		assertTrue(node.getOffset() <= 9 && 9 <= node.getOffset() + node.getLength());
		
		node = results.ast.findNode(0);
		assertTrue(node instanceof Comment);
		assertTrue(node.getOffset() <= 0 && 0 <= node.getOffset() + node.getLength());
	}

	@Test
	public void positionEmptyLine() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value\n" + "\t\n");
		Node node = results.ast.findNode(23);
		assertTrue(node instanceof EmptyLine);
		assertTrue(node.getOffset() <= 23 && 23 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(24);
		assertTrue(node instanceof EmptyLine);
		assertTrue(node.getOffset() <= 24 && 24 <= node.getOffset() + node.getLength());
	}
	
	@Test
	public void positionKey() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value\n" + "\t\n");
		Node node = results.ast.findNode(10);
		assertTrue(node instanceof Key);
		assertTrue(node.getOffset() <= 10 && 10 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(12);
		assertTrue(node instanceof Key);
		assertTrue(node.getOffset() <= 12 && 12 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(13);
		assertTrue(node instanceof Key);
		assertTrue(node.getOffset() <= 13 && 13 <= node.getOffset() + node.getLength());

	}
	
	@Test
	public void positionPair() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value\n" + "\t\n");
		Node node = results.ast.findNode(15);
		assertTrue(node instanceof KeyValuePair);
		assertTrue(node.getOffset() <= 15 && 15 <= node.getOffset() + node.getLength());
	}
	
	@Test
	public void positionValue() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value\n");
		Node node = results.ast.findNode(17);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 17 && 17 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(22);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 22 && 22 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(16);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 16 && 16 <= node.getOffset() + node.getLength());
	}

	@Test
	public void positionValueEofAtEnd() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  = value");
		Node node = results.ast.findNode(22);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 22 && 22 <= node.getOffset() + node.getLength());

		node = results.ast.findNode(16);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 16 && 16 <= node.getOffset() + node.getLength());
	}

	@Test
	public void positionEmptyValue() throws Exception {
		ParseResults results = parser.parse("# Comment\n" + "key  =");
		Node node = results.ast.findNode(16);
		assertTrue(node instanceof Value);
		assertTrue(node.getOffset() <= 16 && 16 <= node.getOffset() + node.getLength());
	}
	
}
