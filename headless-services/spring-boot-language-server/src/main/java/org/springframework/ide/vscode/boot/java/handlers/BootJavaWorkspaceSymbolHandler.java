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

import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.app.FeatureFlag;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.requestmapping.LiveAppURLSymbolProvider;
import org.springframework.ide.vscode.commons.languageserver.util.WorkspaceSymbolHandler;

/**
 * @author Martin Lippert
 */
public class BootJavaWorkspaceSymbolHandler implements WorkspaceSymbolHandler {

	private final SpringSymbolIndex indexer;
	private final LiveAppURLSymbolProvider liveAppSymbolProvider;

	private final BootJavaConfig config;

	public BootJavaWorkspaceSymbolHandler(SpringSymbolIndex indexer, LiveAppURLSymbolProvider liveAppSymbolProvider, BootJavaConfig config) {
		this.indexer = indexer;
		this.liveAppSymbolProvider = liveAppSymbolProvider;
		this.config = config;
	}

	@Override
	public List<? extends WorkspaceSymbol> handle(WorkspaceSymbolParams params) {
		if (params.getQuery() != null && params.getQuery().startsWith("//")) {
			return liveAppSymbolProvider.getSymbols(params.getQuery());
		}
		else if (!config.isFeatureEnabled(FeatureFlag.WORKSPACE_SYMBOLS)) {
			return List.of();
		}
		else {
			return indexer.getAllSymbols(params.getQuery());
		}
	}

}
