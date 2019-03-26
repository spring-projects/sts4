/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.List;

import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentSymbolHandler;

/**
 * @author Martin Lippert
 */
public class BootJavaDocumentSymbolHandler implements DocumentSymbolHandler {

	private SpringSymbolIndex indexer;

	public BootJavaDocumentSymbolHandler(SpringSymbolIndex indexer) {
		this.indexer = indexer;
	}

	@Override
	public List<? extends SymbolInformation> handle(DocumentSymbolParams params) {
		return indexer.getSymbols(params.getTextDocument().getUri());
	}

}
