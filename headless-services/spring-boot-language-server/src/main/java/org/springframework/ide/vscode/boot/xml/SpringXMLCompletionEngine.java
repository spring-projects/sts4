/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.xml.completions.BeanRefCompletionProposalProvider;
import org.springframework.ide.vscode.boot.xml.completions.PropertyNameCompletionProposalProvider;
import org.springframework.ide.vscode.boot.xml.completions.TypeCompletionProposalProvider;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringXMLCompletionEngine implements ICompletionEngine {

	private static final String BEANS_NAMESPACE = "http://www.springframework.org/schema/beans";

	private static final String BEAN_ELEMENT = "bean";
	private static final String CLASS_ATTRIBUTE = "class";

	private static final String PROPERTY_ELEMENT = "property";
	private static final String NAME_ATTRIBUTE = "name";
	private static final String REF_ATTRIBUTE = "ref";

	private final Map<XMLCompletionProviderKey, XMLCompletionProvider> completionProviders;

	public SpringXMLCompletionEngine(SpringXMLLanguageServerComponents springXMLLanguageServerComponents,
			JavaProjectFinder projectFinder, SpringSymbolIndex symbolIndex, SimpleTextDocumentService simpleTextDocumentService) {

		this.completionProviders = new HashMap<>();
		this.completionProviders.put(new XMLCompletionProviderKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, CLASS_ATTRIBUTE), new TypeCompletionProposalProvider(projectFinder, simpleTextDocumentService, true));
		this.completionProviders.put(new XMLCompletionProviderKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, NAME_ATTRIBUTE), new PropertyNameCompletionProposalProvider(projectFinder));
		this.completionProviders.put(new XMLCompletionProviderKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, REF_ATTRIBUTE), new BeanRefCompletionProposalProvider(projectFinder, symbolIndex));
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument doc, int offset) throws Exception {
		String content = doc.get();

		DOMParser parser = DOMParser.getInstance();
		DOMDocument dom = parser.parse(content, "", null);

		DOMNode node = dom.findNodeBefore(offset);

		if (node != null) {
			String namespace = node.getNamespaceURI();

			Scanner scanner = XMLScanner.createScanner(content, node.getStart(), false);
			TokenType token = scanner.scan();
			while (token != TokenType.EOS && scanner.getTokenOffset() <= offset) {
				switch (token) {
				case AttributeValue:
					if (scanner.getTokenOffset() <= offset && offset <= scanner.getTokenEnd()) {
						DOMAttr attributeAt = dom.findAttrAt(offset);

						if (attributeAt != null) {
							XMLCompletionProviderKey key = new XMLCompletionProviderKey(namespace, null, node.getLocalName(), attributeAt.getNodeName());

							if (!this.completionProviders.containsKey(key)) {
								DOMNode parentNode = node.getParentNode();
								String parentNodeName = parentNode != null ? parentNode.getLocalName() : null;
								key = new XMLCompletionProviderKey(namespace, parentNodeName, node.getLocalName(), attributeAt.getNodeName());
							}

							XMLCompletionProvider completionProvider = this.completionProviders.get(key);
							if (completionProvider != null) {
								Collection<ICompletionProposal> completions = completionProvider.getCompletions(doc, namespace, node, attributeAt, scanner, offset);
								return completions;
							}
						}
					}
					break;
				default:
					break;
				}
				token = scanner.scan();
			}
		}
		return Collections.emptyList();
	}

}
