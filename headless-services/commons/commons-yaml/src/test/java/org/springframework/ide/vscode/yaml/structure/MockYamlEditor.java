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
package org.springframework.ide.vscode.yaml.structure;

import static org.junit.Assert.assertTrue;

import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.commons.yaml.ast.YamlASTProvider;
import org.springframework.ide.vscode.commons.yaml.ast.YamlFileAST;
import org.springframework.ide.vscode.commons.yaml.ast.YamlParser;
import org.springframework.ide.vscode.commons.yaml.structure.YamlDocument;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureParser.SRootNode;
import org.springframework.ide.vscode.commons.yaml.structure.YamlStructureProvider;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Node;

/**
 * Yaml editor mock for the tests.
 *
 * @author Alex Boyko
 *
 */
class MockYamlEditor {

	private YamlASTProvider parser;

	private String text;

	public MockYamlEditor(String string) throws Exception {
		this.text = string;
		this.parser = new YamlParser();
	}

	@Override
	public String toString() {
		return "YamlEditor("+text+")";
	}

	public SRootNode parseStructure() throws Exception {
		YamlStructureProvider sp = YamlStructureProvider.DEFAULT;
		TextDocument _doc = new TextDocument(null, getLanguageId());
		_doc.setText(text);
		YamlDocument doc = new YamlDocument(_doc, sp);
		return sp.getStructure(doc);
	}

	protected LanguageId getLanguageId() {
		return LanguageId.YAML;
	}

	public YamlFileAST parse() throws Exception {
		TextDocument _doc = new TextDocument(null, getLanguageId());
		_doc.setText(text);
		return parser.getAST(_doc);
	}

	public String getRawText() {
		return text;
	}

	public String getText() {
		//No cursor support, not needed for these tests.
		return getRawText();
	}

	public int startOf(String snippet) {
		int start = text.indexOf(snippet);
		assertTrue("Snippet not found in editor '"+snippet+"'", start>=0);
		return start;
	}

	public int middleOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start + nodeText.length()/2;
		}
		return -1;
	}

	public int endOf(String nodeText) {
		int start = startOf(nodeText);
		if (start>=0) {
			return start+nodeText.length();
		}
		return -1;
	}

	public String textBetween(int start, int end) {
		return text.substring(start, end);
	}

	public String textUnder(SNode node) throws Exception {
		int start = node.getStart();
		int end = node.getTreeEnd();
		return textBetween(start, end);
	}

	public String textUnder(Node node) throws Exception {
		int start = node.getStartMark().getIndex();
		int end = node.getEndMark().getIndex();
		return textBetween(start, end);
	}

}