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

import java.util.Iterator;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;

/**
 * @author Martin Lippert
 */
public class SpringIndexerXMLNamespaceHandlerBeans implements SpringIndexerXMLNamespaceHandler {

	@Override
	public void processStartElement(IJavaProject project, String docURI, StartElement startElement, SymbolHandler handler) {
		String localPart = startElement.getName().getLocalPart();
		if (localPart != null && "bean".equals(localPart)) {
			createBeanSymbol(project, docURI, startElement, handler);
		}
	}

	private void createBeanSymbol(IJavaProject project, String docURI, StartElement startElement, SymbolHandler handler) {
		String beanID = null;
		String beanClass = null;

		Iterator<?> attributes = startElement.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = (Attribute) attributes.next();

			String name = attribute.getName().getLocalPart();
			if (name != null && name.equals("id")) {
				beanID = attribute.getValue();
			}
			else if (name != null && name.equals("class")) {
				String value = attribute.getValue();
				beanClass = value.substring(value.lastIndexOf(".") + 1);
			}
		}

		if (beanID != null && beanClass != null) {
			Range range = new Range();
			range.setStart(new Position(startElement.getLocation().getLineNumber(), startElement.getLocation().getColumnNumber()));
			range.setEnd(new Position(startElement.getLocation().getLineNumber(), startElement.getLocation().getColumnNumber() + 1));

			Location location = new Location();
			location.setUri(docURI);
			location.setRange(range);

			SymbolInformation symbol = new SymbolInformation("@+ '" + beanID + "' " + beanClass, SymbolKind.Interface, new Location(docURI, range));

			EnhancedSymbolInformation fullSymbol = new EnhancedSymbolInformation(symbol, null);
			handler.addSymbol(project, docURI, fullSymbol);
		}
	}

}
