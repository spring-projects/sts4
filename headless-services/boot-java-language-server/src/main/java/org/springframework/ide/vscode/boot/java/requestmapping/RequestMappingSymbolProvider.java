/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.Map;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.boot.java.handlers.SymbolProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class RequestMappingSymbolProvider implements SymbolProvider {

	private static RequestMappingSymbolProvider symbolProvider;

	public static void register(Map<String, SymbolProvider> symbolProviders) {

		synchronized(RequestMappingSymbolProvider.class) {
			if (symbolProvider == null) {
				symbolProvider = new RequestMappingSymbolProvider();
			}
		}

		symbolProviders.put(Constants.SPRING_REQUEST_MAPPING, symbolProvider);
		symbolProviders.put(Constants.SPRING_GET_MAPPING, symbolProvider);
		symbolProviders.put(Constants.SPRING_POST_MAPPING, symbolProvider);
		symbolProviders.put(Constants.SPRING_PUT_MAPPING, symbolProvider);
		symbolProviders.put(Constants.SPRING_DELETE_MAPPING, symbolProvider);
		symbolProviders.put(Constants.SPRING_PATCH_MAPPING, symbolProvider);
	}

	@Override
	public SymbolInformation getSymbol(Annotation node, TextDocument doc) {
		try {
			SymbolInformation symbol = new SymbolInformation(node.toString(), SymbolKind.Interface,
					new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength())));
			return symbol;
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


}
