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
package org.springframework.ide.vscode.boot.java.snippets;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.util.DocumentRegion;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;

import com.google.common.base.Supplier;

public class JavaSnippetBuilder{

	private Supplier<SnippetBuilder> snippetBuilderFactory;

	public JavaSnippetBuilder(Supplier<SnippetBuilder> snippetBuilderFactory) {
		this.snippetBuilderFactory = snippetBuilderFactory;
	}

	public DocumentEdits createEdit(DocumentRegion query, String template) {
		DocumentEdits edit = new DocumentEdits(query.getDocument());

		edit.replace(query.getStart(), query.getEnd(), template);
		return edit;
	}

}
