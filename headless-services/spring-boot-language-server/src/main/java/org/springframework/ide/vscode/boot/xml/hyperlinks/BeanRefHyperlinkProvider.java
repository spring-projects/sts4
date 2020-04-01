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
package org.springframework.ide.vscode.boot.xml.hyperlinks;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleTextDocumentService;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Alex Boyko
 */
public class BeanRefHyperlinkProvider implements XMLHyperlinkProvider {
	
	private final JavaProjectFinder projectFinder;
	private final SpringSymbolIndex symbolIndex;
	private final SimpleTextDocumentService documents;

	public BeanRefHyperlinkProvider(JavaProjectFinder projectFinder, SpringSymbolIndex symbolIndex, SimpleTextDocumentService documents) {
		this.projectFinder = projectFinder;
		this.symbolIndex = symbolIndex;
		this.documents = documents;
	}

	@Override
	public Location getDefinition(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt) {
		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			final IJavaProject project = foundProject.get();
			List<SymbolInformation> symbols = symbolIndex.getSymbols(data -> symbolsFilter(data, attributeAt.getValue())).collect(Collectors.toList());
			if (!symbols.isEmpty()) {
				for (SymbolInformation symbol : symbols) {
					Location location = symbol.getLocation();
					if (project == documents.get(location.getUri())) {
						return location;
					}
				}
				return symbols.get(0).getLocation();
			}
		}
		return null;
	}
	
	private boolean symbolsFilter(EnhancedSymbolInformation data, String beanId) {
		SymbolAddOnInformation[] additionalInformation = data.getAdditionalInformation();
		if (additionalInformation != null) {
			for (SymbolAddOnInformation info : additionalInformation) {
				if (info instanceof BeansSymbolAddOnInformation) {
					return beanId.equals(((BeansSymbolAddOnInformation)info).getBeanID());
				}
			}
		}
		return false;
	}

}
