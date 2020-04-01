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
package org.springframework.ide.vscode.boot.java.utils;

import java.util.List;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.beans.BeanUtils;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;
import org.springframework.lang.NonNull;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXMLNamespaceHandlerBeans implements SpringIndexerXMLNamespaceHandler {

	@Override
	public void processNode(DOMNode node, IJavaProject project, String docURI, long lastModified, TextDocument document, List<CachedSymbol> generatedSymbols) throws Exception {
		String localName = node.getLocalName();
		if (localName != null && "bean".equals(localName)) {
			createBeanSymbol(node, project, docURI, lastModified, document, generatedSymbols);
		}
	}

	private void createBeanSymbol(DOMNode node, IJavaProject project, String docURI, long lastModified, TextDocument document, List<CachedSymbol> generatedSymbols) throws Exception {
		String beanID = null;
		int symbolStart = 0;
		int symbolEnd = 0;

		String beanClass = null;

		List<DOMAttr> attributes = node.getAttributeNodes();
		for (DOMAttr attribute : attributes) {

			String name = attribute.getName();
			if (name != null && name.equals("id")) {
				beanID = attribute.getValue();

				symbolStart = attribute.getStart();
				symbolEnd = attribute.getEnd();
			}
			else if (name != null && name.equals("class")) {
				String value = attribute.getValue();
				beanClass = value.substring(value.lastIndexOf(".") + 1);

				if (symbolStart == 0 && symbolEnd == 0) {
					symbolStart = attribute.getStart();
					symbolEnd = attribute.getEnd();
				}
			}
		}

		if (beanClass != null) {
			int lineStart = document.getLineOfOffset(symbolStart);
			int lineEnd = document.getLineOfOffset(symbolEnd);

			int startInLine = symbolStart - document.getLineOffset(lineStart);
			int endInLine = symbolEnd - document.getLineOffset(lineEnd);

			Range range = new Range();
			range.setStart(new Position(lineStart, startInLine));
			range.setEnd(new Position(lineEnd, endInLine));

			Location location = new Location();
			location.setUri(docURI);
			location.setRange(range);

			if (beanID == null) {
				beanID = deriveBeanIDFromClass(beanClass);
			}

			SymbolInformation symbol = new SymbolInformation("@+ '" + beanID + "' " + beanClass, SymbolKind.Interface, new Location(docURI, range));
			SymbolAddOnInformation[] addon = new SymbolAddOnInformation[] {new BeansSymbolAddOnInformation(beanID)};

			EnhancedSymbolInformation fullSymbol = new EnhancedSymbolInformation(symbol, addon);

			CachedSymbol cachedSymbol = new CachedSymbol(docURI, lastModified, fullSymbol);
			generatedSymbols.add(cachedSymbol);
		}
	}

	private String deriveBeanIDFromClass(@NonNull String beanClass) {
		return BeanUtils.getBeanNameFromType(beanClass);
	}

}
