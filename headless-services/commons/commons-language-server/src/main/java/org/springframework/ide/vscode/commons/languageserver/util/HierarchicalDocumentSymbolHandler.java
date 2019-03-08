/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import java.util.List;

import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolInformation;

import com.google.common.collect.ImmutableList;

/**
 * Note if you implement HierarchicalDocumentSymbolHandler you must also implement the 'legacy'
 * non-hierarchical handler because this is used as a fallback when client doesn't support
 * hierarchical symbols.
 */
public interface HierarchicalDocumentSymbolHandler extends DocumentSymbolHandler {

	HierarchicalDocumentSymbolHandler NO_SYMBOLS = new HierarchicalDocumentSymbolHandler() {
		@Override
		public List<? extends SymbolInformation> handle(DocumentSymbolParams params) {
			return ImmutableList.of();
		}

		@Override
		public List<? extends DocumentSymbol> handleHierarchic(DocumentSymbolParams params) {
			return ImmutableList.of();
		}
	};

	List<? extends DocumentSymbol> handleHierarchic(DocumentSymbolParams params);

}
