/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import org.eclipse.lsp4j.SymbolInformation;

/**
 * @author Martin Lippert
 */
public class EnhancedSymbolInformation {
	
	private final SymbolInformation symbol;
	private final SymbolAddOnInformation[] additionalInformation;

	public EnhancedSymbolInformation(SymbolInformation symbol, SymbolAddOnInformation[] additionalInformation) {
		this.symbol = symbol;
		this.additionalInformation = additionalInformation;
	}
	
	public SymbolInformation getSymbol() {
		return symbol;
	}
	
	public SymbolAddOnInformation[] getAdditionalInformation() {
		return additionalInformation;
	}

}
