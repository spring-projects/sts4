/*******************************************************************************
 * Copyright (c) 2019, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import org.eclipse.lsp4j.WorkspaceSymbol;
import org.springframework.ide.vscode.boot.index.cache.AbstractIndexCacheable;

public class CachedSymbol extends AbstractIndexCacheable {

	private final long lastModified;
	private final WorkspaceSymbol enhancedSymbol;

	public CachedSymbol(String docURI, long lastModified, WorkspaceSymbol enhancedSymbol) {
		super(docURI);
		this.lastModified = lastModified;
		this.enhancedSymbol = enhancedSymbol;
	}

	public WorkspaceSymbol getEnhancedSymbol() {
		return enhancedSymbol;
	}
	
	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "CachedSymbol [docURI=" + getDocURI() + ", enhancedSymbol=" + enhancedSymbol + "]";
	}
	
}
