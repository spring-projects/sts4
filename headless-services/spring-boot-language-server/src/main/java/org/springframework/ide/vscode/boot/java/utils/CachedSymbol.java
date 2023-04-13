/*******************************************************************************
 * Copyright (c) 2019, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import org.springframework.ide.vscode.boot.java.handlers.EnhancedSymbolInformation;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;

public class CachedSymbol {

	private final String docURI;
	private final long lastModified;
	private final EnhancedSymbolInformation enhancedSymbol;
	private final Bean bean;

	public CachedSymbol(String docURI, long lastModified, EnhancedSymbolInformation enhancedSymbol, Bean bean) {
		this.docURI = docURI;
		this.lastModified = lastModified;
		this.enhancedSymbol = enhancedSymbol;
		this.bean = bean;
	}

	public EnhancedSymbolInformation getEnhancedSymbol() {
		return enhancedSymbol;
	}
	
	public Bean getBean() {
		return bean;
	}

	public String getDocURI() {
		return docURI;
	}

	public long getLastModified() {
		return lastModified;
	}

	@Override
	public String toString() {
		return "CachedSymbol [docURI=" + docURI + ", enhancedSymbol=" + enhancedSymbol + "]";
	}

	
}
