/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ide.vscode.boot.java.links.JavaElementLocationProvider;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.boot.metadata.types.TypeUtil;
import org.springframework.ide.vscode.boot.properties.hover.PropertiesDefinitionCalculator;
import org.springframework.ide.vscode.commons.languageserver.util.DefinitionHandler;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.FuzzyMap;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.stereotype.Component;

@Component
public class PropertiesJavaDefinitionHandler implements DefinitionHandler {

	@Autowired
	private SimpleTextDocumentService documents;

	@Autowired
	private JavaElementLocationProvider javaDocumentLocationProvider;

	@Autowired
	private BootLanguageServerParams params;

	@Override
	public List<Location> handle(TextDocumentPositionParams position) {
		try {
			TextDocument doc = documents.get(position);
			TypeUtil typeUtil = params.typeUtilProvider.getTypeUtil(doc);
			FuzzyMap<PropertyInfo> index = params.indexProvider.getIndex(doc);
			int offset;
			offset = doc.toOffset(position.getPosition());
			return new PropertiesDefinitionCalculator(javaDocumentLocationProvider, index, typeUtil, doc, offset).calculate();
		} catch (BadLocationException e) {
			return ImmutableList.of();
		}
	}

}
