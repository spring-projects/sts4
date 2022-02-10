/*******************************************************************************
 * Copyright (c) 2018, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.utils;

import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.marker.Range;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class DefaultSymbolProvider {

	public static SymbolInformation provideDefaultSymbol(Annotation node, TextDocument doc) throws Exception {
		Range r = ORAstUtils.getRange(node);
		SymbolInformation symbol = new SymbolInformation(node.toString(), SymbolKind.Interface,
				new Location(doc.getUri(), doc.toRange(r.getStart().getOffset(), r.length())));
		return symbol;
	}

}
