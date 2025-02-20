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
package org.springframework.ide.vscode.commons.protocol.spring;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.WorkspaceSymbol;

public class SimpleSymbolElement extends AbstractSpringIndexElement implements SymbolElement {
	
	private final WorkspaceSymbol symbol;

	public SimpleSymbolElement(WorkspaceSymbol symbol) {
		this.symbol = symbol;
	}

	@Override
	public DocumentSymbol getDocumentSymbol() {
		DocumentSymbol documentSymbol = new DocumentSymbol();
		documentSymbol.setName(symbol.getName());
		documentSymbol.setKind(symbol.getKind());
		documentSymbol.setTags(symbol.getTags());
		
		Range range = symbol.getLocation().isLeft() ? symbol.getLocation().getLeft().getRange() : new Range(); 
		documentSymbol.setRange(range);
		documentSymbol.setSelectionRange(range);

		return documentSymbol;
	}

}
