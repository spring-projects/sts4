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
import org.springframework.ide.vscode.commons.protocol.spring.SpringIndexElement;
import org.springframework.ide.vscode.commons.protocol.spring.SymbolElement;

public class SpringIndexToSymbolsConverter {

	public static List<DocumentSymbol> createDocumentSymbols(List<SpringIndexElement> indexElements) {
		List<DocumentSymbol> result = new ArrayList<>();
		
		for (SpringIndexElement indexElement : indexElements) {
			result.addAll(createSymbol(indexElement));
		}
		
		return result;
	}

	private static List<DocumentSymbol> createSymbol(SpringIndexElement indexElement) {
		
		List<DocumentSymbol> subTreeSymbols = new ArrayList<>();
		List<SpringIndexElement> children = indexElement.getChildren();

		if (children != null && children.size() > 0) {
			for (SpringIndexElement child : children) {
				List<DocumentSymbol> childSymbols = createSymbol(child);
				if (childSymbols != null) {
					subTreeSymbols.addAll(childSymbols);
				}
			}
		}
		
		if (indexElement instanceof SymbolElement symbolElement) {
			DocumentSymbol documentSymbol = symbolElement.getDocumentSymbol();
			if (subTreeSymbols.size() > 0) {
				documentSymbol.setChildren(subTreeSymbols);
			}
			
			return List.of(documentSymbol);
		}
		else {
//			symbol = new DocumentSymbol(indexElement.toString(), SymbolKind.String,
//					new Range(new Position(), new Position()),
//					new Range(new Position(), new Position()));
			return subTreeSymbols;
		}
	}

}
