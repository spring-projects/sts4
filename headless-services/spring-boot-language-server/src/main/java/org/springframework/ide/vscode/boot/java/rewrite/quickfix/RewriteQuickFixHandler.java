/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite.quickfix;

import java.util.List;

import org.eclipse.lsp4j.WorkspaceEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixHandler;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public abstract class RewriteQuickFixHandler implements QuickfixHandler {
	
	final static private Gson gson = new Gson();
	
	@Override
	public QuickfixEdit createEdits(Object p) {
		if (p instanceof JsonElement) {
			List<?> l = gson.fromJson((JsonElement) p, List.class);
			return new QuickfixEdit(perform(l), null);
		}
		return null;
	}

	abstract protected WorkspaceEdit perform(List<?> l);
	
}
