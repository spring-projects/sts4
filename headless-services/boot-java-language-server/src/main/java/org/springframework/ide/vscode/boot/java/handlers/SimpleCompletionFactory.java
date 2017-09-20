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
package org.springframework.ide.vscode.boot.java.handlers;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class SimpleCompletionFactory {

	public ICompletionProposal simpleProposal(IDocument doc, int offset, String query, CompletionItemKind kind, String value, String detail, Renderable info) {
		DocumentEdits edits = new DocumentEdits(doc);
		edits.replace(offset-query.length(), offset, value);
		return new ICompletionProposal() {
			@Override
			public DocumentEdits getTextEdit() {
				return edits;
			}

			@Override
			public String getLabel() {
				return value;
			}

			@Override
			public CompletionItemKind getKind() {
				return kind;
			}

			@Override
			public Renderable getDocumentation() {
				return info;
			}

			@Override
			public String getDetail() {
				return detail;
			}
		};
	}
}
