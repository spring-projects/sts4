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
package org.springframework.ide.vscode.boot.java.utils;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class DefaultSymbolProvider {

	public static WorkspaceSymbol provideDefaultSymbol(Annotation node, TextDocument doc) throws Exception {
		WorkspaceSymbol symbol = new WorkspaceSymbol(node.toString(), SymbolKind.Interface,
				Either.forLeft(new Location(doc.getUri(), doc.toRange(node.getStartPosition(), node.getLength()))));
		return symbol;
	}

}
