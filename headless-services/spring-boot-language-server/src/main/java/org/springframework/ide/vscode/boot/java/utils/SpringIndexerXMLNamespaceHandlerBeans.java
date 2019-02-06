/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import java.util.List;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXMLNamespaceHandlerBeans implements SpringIndexerXMLNamespaceHandler {

	@Override
	public void processNode(DOMNode node, IJavaProject project, String docURI, TextDocument document, SymbolHandler symbolHandler) throws Exception {
		String localName = node.getLocalName();
		if (localName != null && "bean".equals(localName)) {
			createBeanSymbol(node, project, docURI, document, symbolHandler);
		}
	}

	private void createBeanSymbol(DOMNode node, IJavaProject project, String docURI, TextDocument document, SymbolHandler symbolHandler) throws Exception {
		String beanID = null;
		int beanIDStart = 0;
		int beanIDEnd = 0;

		String beanClass = null;

		List<DOMAttr> attributes = node.getAttributeNodes();
		for (DOMAttr attribute : attributes) {

			String name = attribute.getName();
			if (name != null && name.equals("id")) {
				beanID = attribute.getValue();
				beanIDStart = attribute.getStart();
				beanIDEnd = attribute.getEnd();
			}
			else if (name != null && name.equals("class")) {
				String value = attribute.getValue();
				beanClass = value.substring(value.lastIndexOf(".") + 1);
			}
		}

		if (beanID != null && beanClass != null) {
			int lineStart = document.getLineOfOffset(beanIDStart);
			int lineEnd = document.getLineOfOffset(beanIDEnd);

			int startInLine = beanIDStart - document.getLineOffset(lineStart);
			int endInLine = beanIDEnd - document.getLineOffset(lineEnd);

			Range range = new Range();
			range.setStart(new Position(lineStart + 1, startInLine));
			range.setEnd(new Position(lineEnd + 1, endInLine));

			Location location = new Location();
			location.setUri(docURI);
			location.setRange(range);

			SymbolInformation symbol = new SymbolInformation("@+ '" + beanID + "' " + beanClass, SymbolKind.Interface, new Location(docURI, range));

			EnhancedSymbolInformation fullSymbol = new EnhancedSymbolInformation(symbol, null);
			symbolHandler.addSymbol(project, docURI, fullSymbol);
		}
	}

}
