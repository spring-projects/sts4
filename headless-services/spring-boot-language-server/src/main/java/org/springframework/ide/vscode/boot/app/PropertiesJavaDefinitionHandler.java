/*******************************************************************************
 * Copyright (c) 2018, 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.DefinitionParams;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.java.links.SourceLinks;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.types.Type;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.properties.hover.PropertiesDefinitionCalculator;
import org.springframework.ide.vscode.boot.properties.hover.PropertyFinder;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IMember;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Key;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Node;
import org.springframework.ide.vscode.java.properties.parser.PropertiesAst.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class PropertiesJavaDefinitionHandler implements DefinitionHandler, LanguageSpecific {
		
	private static final Logger log = LoggerFactory.getLogger(PropertiesJavaDefinitionHandler.class);

	@Autowired
	private SimpleTextDocumentService documents;

	@Autowired
	private JavaElementLocationProvider javaElementLocationProvider;

	@Autowired
	private SourceLinks sourceLinks;

	@Autowired
	private BootLanguageServerParams params;

	@Override
	public List<LocationLink> handle(DefinitionParams definitionParams) {
		try {
			TextDocument doc = documents.get(definitionParams);
			TypeUtil typeUtil = params.typeUtilProvider.getTypeUtil(sourceLinks, doc);
			FuzzyMap<PropertyInfo> index = params.indexProvider.getIndex(doc).getProperties();
			int offset;
			offset = doc.toOffset(definitionParams.getPosition());
			return getDefinitions(index, typeUtil, doc, offset);
		} catch (BadLocationException e) {
			return ImmutableList.of();
		}
	}

	private List<LocationLink> getDefinitions(FuzzyMap<PropertyInfo> index, TypeUtil typeUtil, TextDocument doc, int offset) {
		IJavaProject project = typeUtil.getJavaProject();
		PropertyFinder propertyFinder = new PropertyFinder(index, typeUtil, doc, offset);
		Node node = propertyFinder.findNode();
		try {
			Range selectionRange = doc.toRange(node.getOffset(), node.getLength());
			if (node instanceof Key) {
				String propertyKey = ((Key) node).decode();
				Collection<IMember> propertyJavaElements = PropertiesDefinitionCalculator.getPropertyJavaElements(typeUtil, propertyFinder, project, propertyKey);

				// Attempt to highlight only chunk of the key for which definition is found
				PropertyInfo propertyInfo = propertyFinder.findBestHoverMatch(propertyKey);
				if (propertyInfo != null) {
					Range range = adjustedHighlightRangeForKey(doc, selectionRange, propertyInfo);
					return PropertiesDefinitionCalculator.getLocations(javaElementLocationProvider, project, propertyJavaElements).stream()
							.map(l -> new LocationLink(l.getUri(), l.getRange(), l.getRange(), range))
							.collect(Collectors.toList());
				}
			} else if (node instanceof Value) {
				Value value = (Value) node;
				Key key = value.getParent().getKey();
				Type type = PropertiesDefinitionCalculator.getPropertyType(propertyFinder, key.decode());
				if (type != null) {
					// Trim spaces from the value node text
					Range range = trimHighlightRange(selectionRange, doc);
					return PropertiesDefinitionCalculator.getValueDefinitionLocations(javaElementLocationProvider, typeUtil, type, value.decode()).stream()
							.map(l -> new LocationLink(l.getUri(), l.getRange(), l.getRange(), range))
							.collect(Collectors.toList());
				}
			}
		} catch (BadLocationException e) {
			log.error("", e);
		}
		return ImmutableList.of();
	}
	
	private Range adjustedHighlightRangeForKey(TextDocument doc, Range range, PropertyInfo propertyInfo) throws BadLocationException {
		Range adjustedRange = trimHighlightRange(range, doc);
		int start = doc.toOffset(range.getStart());
		String id = propertyInfo.getId();
		if (id.equals(doc.get(start, id.length()))) {
			adjustedRange.setEnd(doc.toPosition(start + id.length()));
		}
		return adjustedRange;
	}
	
	private Range trimHighlightRange(Range range, TextDocument doc) throws BadLocationException {
		int start = doc.toOffset(range.getStart());
		for (; start < doc.getLength() && Character.isWhitespace(doc.getChar(start)); start++) {}
		int end = start;
		for (; end < doc.getLength() && !Character.isWhitespace(doc.getChar(end)); end++) {}
		return doc.toRange(start, end - start);
	}
	
	@Override
	public Collection<LanguageId> supportedLanguages() {
		return ImmutableList.of(LanguageId.BOOT_PROPERTIES);
	}

}
