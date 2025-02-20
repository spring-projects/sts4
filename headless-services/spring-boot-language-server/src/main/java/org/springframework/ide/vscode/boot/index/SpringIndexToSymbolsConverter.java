/*******************************************************************************
 * Copyright (c) 2025 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.index;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.commons.protocol.spring.SymbolElement;

public class SpringIndexToSymbolsConverter {

	public static List<DocumentSymbol> createDocumentSymbols(List<SpringIndexElement> indexElements) {
		List<DocumentSymbol> result = new ArrayList<>();
		
		for (SpringIndexElement indexElement : indexElements) {
			result.add(createSymbol(indexElement));
		}
		
		return result;
	}

	private static DocumentSymbol createSymbol(SpringIndexElement indexElement) {
		
		DocumentSymbol symbol = null;
		if (indexElement instanceof SymbolElement symbolElement) {
			symbol = symbolElement.getDocumentSymbol();
		}
		else {
			symbol = new DocumentSymbol(indexElement.toString(), SymbolKind.String,
					new Range(new Position(), new Position()),
					new Range(new Position(), new Position()));
		}
		
		List<SpringIndexElement> children = indexElement.getChildren();
		if (children != null && children.size() > 0) {
			List<DocumentSymbol> childSymbols = new ArrayList<>();
			
			for (SpringIndexElement child : children) {
				DocumentSymbol childSymbol = createSymbol(child);
				if (childSymbol != null) {
					childSymbols.add(childSymbol);
				}
			}
			
			if (childSymbols.size() > 0) {
				symbol.setChildren(childSymbols);
			}
		}
		
		return symbol;
	}

}
