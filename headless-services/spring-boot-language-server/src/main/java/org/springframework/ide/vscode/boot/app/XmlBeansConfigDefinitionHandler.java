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
package org.springframework.ide.vscode.boot.app;

import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEANS_NAMESPACE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEAN_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.CLASS_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.NAME_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.PROPERTY_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.REF_ATTRIBUTE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMDocument;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.DOMParser;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.eclipse.lsp4xml.dom.parser.TokenType;
import org.eclipse.lsp4xml.dom.parser.XMLScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.xml.XMLElementKey;
import org.springframework.ide.vscode.boot.xml.hyperlinks.BeanRefHyperlinkProvider;
import org.springframework.ide.vscode.boot.xml.hyperlinks.JavaTypeHyperlinkProvider;
import org.springframework.ide.vscode.boot.xml.hyperlinks.PropertyNameHyperlinkProvider;
import org.springframework.ide.vscode.boot.xml.hyperlinks.XMLHyperlinkProvider;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * 
 * @author Alex Boyko
 *
 */
@Component
public class XmlBeansConfigDefinitionHandler implements DefinitionHandler, LanguageSpecific {
	
	private static final Logger log = LoggerFactory.getLogger(XmlBeansConfigDefinitionHandler.class);

	private final Map<XMLElementKey, List<? extends XMLHyperlinkProvider>> hyperlinkProviders;

	private final SimpleTextDocumentService documents;
	private final BootJavaConfig config;
	
	public XmlBeansConfigDefinitionHandler(SimpleTextDocumentService documents,
			BootJavaConfig config,
			JavaElementLocationProvider locationProvider,
			SpringSymbolIndex symbolIndex,
			BootLanguageServerParams serverParams) {
		this.documents = documents;
		this.config = config;
		JavaProjectFinder projectFinder = serverParams.projectFinder;
		
		hyperlinkProviders = new HashMap<>();
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, CLASS_ATTRIBUTE), Arrays.asList(new JavaTypeHyperlinkProvider(projectFinder, locationProvider)));
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, NAME_ATTRIBUTE), Arrays.asList(new PropertyNameHyperlinkProvider(projectFinder, locationProvider)));
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, REF_ATTRIBUTE), Arrays.asList(new BeanRefHyperlinkProvider(projectFinder, symbolIndex, documents)));

	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return Arrays.asList(LanguageId.XML);
	}

	@Override
	public List<Location> handle(TextDocumentPositionParams position) {
		try {
			if (config.isSpringXMLSupportEnabled() && config.areXmlHyperlinksEnabled()) {
				TextDocument doc = documents.get(position);
				if (doc != null) {
					String content = doc.get();
	
					DOMParser parser = DOMParser.getInstance();
					DOMDocument dom = parser.parse(content, "", null);
					
					int offset = doc.toOffset(position.getPosition());
	
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
										XMLElementKey key = new XMLElementKey(namespace, null, node.getLocalName(), attributeAt.getNodeName());
	
										if (!hyperlinkProviders.containsKey(key)) {
											DOMNode parentNode = node.getParentNode();
											String parentNodeName = parentNode != null ? parentNode.getLocalName() : null;
											key = new XMLElementKey(namespace, parentNodeName, node.getLocalName(), attributeAt.getNodeName());
										}
	
										List<? extends XMLHyperlinkProvider> providers = hyperlinkProviders.get(key);
										if (providers != null) {
											ImmutableList.Builder<Location> listBuilder = ImmutableList.builder();
											for (XMLHyperlinkProvider provider : providers) {
												Location location = provider.getDefinition(doc, namespace, node, attributeAt);
												if (location != null) {
													listBuilder.add(location);
												}
											}
											return listBuilder.build();
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
				}
			}
		} catch (Exception e) {
			log.error("{}", e);
		}
		return null;
	}

}
