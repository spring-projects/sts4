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

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.requestmapping.LiveAppURLSymbolProvider;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;

/**
 * @author Martin Lippert
 */
public class BootJavaWorkspaceSymbolHandler implements WorkspaceSymbolHandler {

	private final SpringSymbolIndex indexer;
	private final LiveAppURLSymbolProvider liveAppSymbolProvider;

	public BootJavaWorkspaceSymbolHandler(SpringSymbolIndex indexer, LiveAppURLSymbolProvider liveAppSymbolProvider) {
		this.indexer = indexer;
		this.liveAppSymbolProvider = liveAppSymbolProvider;
	}

	@Override
	public List<? extends SymbolInformation> handle(WorkspaceSymbolParams params) {
		if (params.getQuery() != null && params.getQuery().startsWith("//")) {
			return liveAppSymbolProvider.getSymbols(params.getQuery());
		}
		else {
			return indexer.getAllSymbols(params.getQuery());
		}
	}

}
