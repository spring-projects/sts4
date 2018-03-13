/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.DocumentHighlight;
import org.springframework.ide.vscode.boot.java.BootJavaLanguageServerComponents;
import org.springframework.ide.vscode.boot.java.handlers.HighlightProvider;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Martin Lippert
 */
public class WebfluxRouteHighlightProdivder implements HighlightProvider {

	public WebfluxRouteHighlightProdivder(BootJavaLanguageServerComponents server) {
	}

	@Override
	public void provideHighlights(TextDocument document, CompilationUnit cu,
			List<DocumentHighlight> resultAccumulator) {
	}

}
