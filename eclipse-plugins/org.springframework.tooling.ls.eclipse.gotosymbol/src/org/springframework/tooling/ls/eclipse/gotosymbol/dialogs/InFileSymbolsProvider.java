/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.gotosymbol.dialogs;

import java.util.Collection;

import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.ui.texteditor.ITextEditor;

public class InFileSymbolsProvider implements SymbolsProvider {
	
	private ITextEditor target;

	public InFileSymbolsProvider(ITextEditor target) {
		super();
		this.target = target;
	}

	@Override
	public Collection<SymbolInformation> fetchFor(String query) {
		// TODO Auto-generated method stub
		return null;
	}

}
