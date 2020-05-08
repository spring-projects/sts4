/*******************************************************************************
 * Copyright (c) 2019, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.ALIAS_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.ARG_TYPE_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEANS_NAMESPACE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEAN_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.BEAN_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.CLASS_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.COMPONENT_SCAN_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.CONSTRUCTOR_ARG_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.CONTEXT_NAMESPACE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.DEPENDS_ON_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.ENTRY_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.FACTORY_BEAN_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.IDREF_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.KEY_REF_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.KEY_TYPE_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.LOOKUP_METHOD_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.MATCH_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.NAME_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.NAME_GENERATOR_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.PARENT_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.PROPERTY_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.REF_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.REF_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.REPLACED_METHOD_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.REPLACER_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.SCOPE_RESOLVER_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.TYPE_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.VALUE_ELEMENT;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.VALUE_REF_ATTRIBUTE;
import static org.springframework.ide.vscode.boot.xml.XmlConfigConstants.VALUE_TYPE_ATTRIBUTE;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.DOMParser;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lemminx.dom.parser.TokenType;
import org.eclipse.lemminx.dom.parser.XMLScanner;
import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
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
 * @author Alex Boyko
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
		
		JavaTypeHyperlinkProvider javaTypeHyperlinkProvider = new JavaTypeHyperlinkProvider(projectFinder, locationProvider);
		PropertyNameHyperlinkProvider propertyNameHyperlinkProvider = new PropertyNameHyperlinkProvider(projectFinder, locationProvider);
		BeanRefHyperlinkProvider beanRefHyperlinkProvider = new BeanRefHyperlinkProvider(projectFinder, symbolIndex, documents);
		
		List<JavaTypeHyperlinkProvider> typeHandlersOnly = Arrays.asList(javaTypeHyperlinkProvider);
		List<PropertyNameHyperlinkProvider> propertyNameHandlers = Arrays.asList(propertyNameHyperlinkProvider);
		List<BeanRefHyperlinkProvider> beanRefHandlersOnly = Arrays.asList(beanRefHyperlinkProvider);
		
		hyperlinkProviders = new HashMap<>();
		
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, CLASS_ATTRIBUTE), typeHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, CONSTRUCTOR_ARG_ELEMENT, TYPE_ATTRIBUTE), typeHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, ARG_TYPE_ELEMENT, MATCH_ATTRIBUTE), typeHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, VALUE_ELEMENT, TYPE_ATTRIBUTE), typeHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, null, VALUE_TYPE_ATTRIBUTE), typeHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, null, KEY_TYPE_ATTRIBUTE), typeHandlersOnly);
		
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, PARENT_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, DEPENDS_ON_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, BEAN_ELEMENT, FACTORY_BEAN_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, REF_ELEMENT, BEAN_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, IDREF_ELEMENT, BEAN_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, CONSTRUCTOR_ARG_ELEMENT, REF_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, ALIAS_ELEMENT, NAME_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, REPLACED_METHOD_ELEMENT, REPLACER_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, ENTRY_ELEMENT, VALUE_REF_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, ENTRY_ELEMENT, KEY_REF_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, null, LOOKUP_METHOD_ELEMENT, BEAN_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, REF_ATTRIBUTE), beanRefHandlersOnly);

		hyperlinkProviders.put(new XMLElementKey(BEANS_NAMESPACE, BEAN_ELEMENT, PROPERTY_ELEMENT, NAME_ATTRIBUTE), propertyNameHandlers);
		
		hyperlinkProviders.put(new XMLElementKey(CONTEXT_NAMESPACE, null, COMPONENT_SCAN_ELEMENT, NAME_GENERATOR_ATTRIBUTE), beanRefHandlersOnly);
		hyperlinkProviders.put(new XMLElementKey(CONTEXT_NAMESPACE, null, COMPONENT_SCAN_ELEMENT, SCOPE_RESOLVER_ATTRIBUTE), beanRefHandlersOnly);

	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return Arrays.asList(LanguageId.XML);
	}

	@Override
	public List<LocationLink> handle(DefinitionParams params) {
		try {
			if (config.isSpringXMLSupportEnabled() && config.areXmlHyperlinksEnabled()) {
				TextDocument doc = documents.get(params);
				if (doc != null) {
					String content = doc.get();
	
					DOMParser parser = DOMParser.getInstance();
					DOMDocument dom = parser.parse(content, "", null);
					
					int offset = doc.toOffset(params.getPosition());
	
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
											ImmutableList.Builder<LocationLink> listBuilder = ImmutableList.builder();
											for (XMLHyperlinkProvider provider : providers) {
												Location location = provider.getDefinition(doc, namespace, node, attributeAt);
												if (location != null) {
													int start = attributeAt.getNodeAttrValue().getStart() + 1;
													int end = attributeAt.getNodeAttrValue().getEnd() - 1;
													listBuilder.add(new LocationLink(location.getUri(),
															location.getRange(), location.getRange(),
															doc.toRange(start, Math.max(0, end - start))));
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
