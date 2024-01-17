/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMRange;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.IndentUtil;
import org.springframework.ide.vscode.commons.languageserver.util.PrefixFinder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class NamespaceCompletionProvider {
	
	private static PrefixFinder PREFIX_FINDER = new PrefixFinder() {
		@Override
		protected boolean isPrefixChar(char c) {
			return Character.isAlphabetic(c) || c == ':';
		}
	};

	public static Collection<ICompletionProposal> createNamespaceCompletionProposals(TextDocument doc, int offset, TokenType token, DOMNode node) {
		String prefix = PREFIX_FINDER.getPrefix(doc, offset);

		if (prefix != null) {
			Collection<ICompletionProposal> result = new ArrayList<>();
			
			NamespaceInformation[] namespaces = getNamespaces();
			for (NamespaceInformation namespace : namespaces) {
				if (namespace.getNamespaceURI().startsWith(prefix)) {
					result.add(createCompletionProposal(namespace, prefix, doc, offset, token, node));
				}
			}
			
			return result;
		}
		else {
			return Collections.emptyList();
		}
	}
	
	private static ICompletionProposal createCompletionProposal(NamespaceInformation namespace, String prefix, TextDocument doc, int offset, TokenType token, DOMNode node) {

		IndentUtil indentUtil = new IndentUtil(doc);

		// main edit (inserts the namespace attribute itself)
		DocumentEdits edits = new DocumentEdits(doc, false);
		String startIndent = indentUtil.getReferenceIndent(offset, doc);
		edits.replace(offset - prefix.length(), offset, namespace.getNamespaceURI() + "\n" + startIndent);

		// additional edit (inserts the namespace location to an existing xsi:schemaLocation attribute)
		DocumentEdits additionalEdits = null;
		DOMAttr attributeNode = node.getAttributeNode("xsi:schemaLocation");
		if (attributeNode != null) {
			DOMRange range = attributeNode.getNodeAttrValue();

			String locationIndent = indentUtil.getReferenceIndent(range.getEnd(), doc);
			String locationIndentAtStart = indentUtil.getReferenceIndent(range.getStart(), doc);

			if (locationIndent.length() == locationIndentAtStart.length()) {
				locationIndent = locationIndent + locationIndent;
			}

			String additionalInsertText = "\n"
					+ locationIndent
					+ namespace.getNamespaceLocation();

			additionalEdits = new DocumentEdits(doc, false);
			additionalEdits.insert(range.getEnd() - 1, additionalInsertText);
		}

		Renderable documentation = Renderables.text(namespace.getDetails());
		return new GenericXMLCompletionProposal(namespace.getLabel(), CompletionItemKind.Text, edits, namespace.getDetails(), documentation, 1.0d, namespace.getNamespaceURI(), additionalEdits);
	}
	
	public static NamespaceInformation[] getNamespaces() {
		return NAMESPACES;
	}
	
	public static class NamespaceInformation {
		
		private final String label;
		private final String details;
		private final String namespaceURI;
		private final String namespaceLocation;
		
		public NamespaceInformation(String label, String details, String namespaceURI,
				String namespaceLocation) {
			this.label = label;
			this.details = details;
			this.namespaceURI = namespaceURI;
			this.namespaceLocation = namespaceLocation;
		}
		
		public String getLabel() {
			return label;
		}
		
		public String getDetails() {
			return details;
		}
		
		public String getNamespaceURI() {
			return namespaceURI;
		}
		
		public String getNamespaceLocation() {
			return namespaceLocation;
		}
		
	}
	
	private static final NamespaceInformation[] NAMESPACES = {
			
			// core framework
			new NamespaceInformation(
					"http://www.springframework.org/schema/beans",
					"http://www.springframework.org/schema/beans",
					"xmlns:beans=\"http://www.springframework.org/schema/beans\"",
					"http://www.springframework.org/schema/beans https://www.springframework.org/schema/beans/spring-beans.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/context",
					"http://www.springframework.org/schema/context",
					"xmlns:context=\"http://www.springframework.org/schema/context\"",
					"http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/tx",
					"http://www.springframework.org/schema/tx",
					"xmlns:tx=\"http://www.springframework.org/schema/tx\"",
					"http://www.springframework.org/schema/tx https://www.springframework.org/schema/tx/spring-tx.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/aop",
					"http://www.springframework.org/schema/aop",
					"xmlns:aop=\"http://www.springframework.org/schema/aop\"",
					"http://www.springframework.org/schema/aop https://www.springframework.org/schema/aop/spring-aop.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/oxm",
					"http://www.springframework.org/schema/oxm",
					"xmlns:oxm=\"http://www.springframework.org/schema/oxm\"",
					"http://www.springframework.org/schema/oxm https://www.springframework.org/schema/oxm/spring-oxm.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/jms",
					"http://www.springframework.org/schema/jms",
					"xmlns:jms=\"http://www.springframework.org/schema/jms\"",
					"http://www.springframework.org/schema/jms https://www.springframework.org/schema/jms/spring-jms.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/jdbc",
					"http://www.springframework.org/schema/jdbc",
					"xmlns:jdbc=\"http://www.springframework.org/schema/jdbc\"",
					"http://www.springframework.org/schema/jdbc https://www.springframework.org/schema/jdbc/spring-jdbc.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/tool",
					"http://www.springframework.org/schema/tool",
					"xmlns:tool=\"http://www.springframework.org/schema/tool\"",
					"http://www.springframework.org/schema/tool https://www.springframework.org/schema/tool/spring-tool.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/util",
					"http://www.springframework.org/schema/util",
					"xmlns:util=\"http://www.springframework.org/schema/util\"",
					"http://www.springframework.org/schema/util https://www.springframework.org/schema/util/spring-util.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/mvc",
					"http://www.springframework.org/schema/mvc",
					"xmlns:mvc=\"http://www.springframework.org/schema/mvc\"",
					"http://www.springframework.org/schema/mvc https://www.springframework.org/schema/mvc/spring-mvc.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/jee",
					"http://www.springframework.org/schema/jee",
					"xmlns:jee=\"http://www.springframework.org/schema/jee\"",
					"http://www.springframework.org/schema/jee https://www.springframework.org/schema/jee/spring-jee.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/lang",
					"http://www.springframework.org/schema/lang",
					"xmlns:lang=\"http://www.springframework.org/schema/lang\"",
					"http://www.springframework.org/schema/lang https://www.springframework.org/schema/lang/spring-lang.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/task",
					"http://www.springframework.org/schema/task",
					"xmlns:task=\"http://www.springframework.org/schema/task\"",
					"http://www.springframework.org/schema/task https://www.springframework.org/schema/task/spring-task.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/cache",
					"http://www.springframework.org/schema/cache",
					"xmlns:cache=\"http://www.springframework.org/schema/cache\"",
					"http://www.springframework.org/schema/cache https://www.springframework.org/schema/cache/spring-cache.xsd"),
			new NamespaceInformation(
					"http://www.springframework.org/schema/websocket",
					"http://www.springframework.org/schema/websocket",
					"xmlns:websocket=\"http://www.springframework.org/schema/websocket\"",
					"http://www.springframework.org/schema/websocket https://www.springframework.org/schema/websocket/spring-websocket.xsd"),

			// security
			new NamespaceInformation(
					"http://www.springframework.org/schema/security",
					"http://www.springframework.org/schema/security",
					"xmlns:security=\"http://www.springframework.org/schema/security\"",
					"http://www.springframework.org/schema/security https://www.springframework.org/schema/security/spring-security.xsd"),
			
	};

}
