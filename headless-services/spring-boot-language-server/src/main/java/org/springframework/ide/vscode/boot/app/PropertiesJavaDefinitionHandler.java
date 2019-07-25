/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
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

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
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

@Component
public class PropertiesJavaDefinitionHandler implements DefinitionHandler, LanguageSpecific {

	@Autowired
	private SimpleTextDocumentService documents;

	@Autowired
	private JavaElementLocationProvider javaElementLocationProvider;

	@Autowired
	private SourceLinks sourceLinks;

	@Autowired
	private BootLanguageServerParams params;

	@Override
	public List<Location> handle(TextDocumentPositionParams position) {
		try {
			TextDocument doc = documents.get(position);
			TypeUtil typeUtil = params.typeUtilProvider.getTypeUtil(sourceLinks, doc);
			FuzzyMap<PropertyInfo> index = params.indexProvider.getIndex(doc);
			int offset;
			offset = doc.toOffset(position.getPosition());
			return getDefinitions(index, typeUtil, doc, offset);
		} catch (BadLocationException e) {
			return ImmutableList.of();
		}
	}

	private List<Location> getDefinitions(FuzzyMap<PropertyInfo> index, TypeUtil typeUtil, TextDocument doc, int offset) {
		IJavaProject project = typeUtil.getJavaProject();
		PropertyFinder propertyFinder = new PropertyFinder(index, typeUtil, doc, offset);
		Node node = propertyFinder.findNode();
		if (node instanceof Key) {
			Collection<IMember> propertyJavaElements = PropertiesDefinitionCalculator.getPropertyJavaElements(typeUtil, propertyFinder, project, ((Key) node).decode());
			return PropertiesDefinitionCalculator.getLocations(javaElementLocationProvider, project, propertyJavaElements);
		} else if (node instanceof Value) {
			Value value = (Value) node;
			Key key = value.getParent().getKey();
			Type type = PropertiesDefinitionCalculator.getPropertyType(propertyFinder, key.decode());
			if (type != null) {
				return PropertiesDefinitionCalculator.getValueDefinitionLocations(javaElementLocationProvider, typeUtil, type, value.decode());
			}
		}
		return ImmutableList.of();
	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return ImmutableList.of(LanguageId.BOOT_PROPERTIES);
	}

}
