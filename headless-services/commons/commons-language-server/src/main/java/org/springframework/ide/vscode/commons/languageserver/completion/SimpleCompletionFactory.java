/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class SimpleCompletionFactory {


	public static class SimpleProposal implements ICompletionProposal{

		private DocumentEdits edits;
		private CompletionItemKind kind;
		private Renderable info;
		private String detail;
		private String label;

		public SimpleProposal(DocumentEdits edits,  CompletionItemKind kind, Renderable info,
				String detail, String label) {
			this.edits = edits;
			this.kind = kind;
			this.info = info;
			this.detail = detail;
			this.label = label;
		}

		public SimpleProposal setLabel(String label) {
			this.label = label;
			return this;
		}

		@Override
		public String getLabel() {
			return label;
		}

		@Override
		public DocumentEdits getTextEdit() {
			return edits;
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

		@Override
		public String toString() {
			return "SimpleProposal("+label+")";
		}

	}

	public static SimpleProposal simpleProposal(DocumentRegion query, CompletionItemKind kind, String value, String detail, Renderable info) {
		return simpleProposal(query.getDocument(), query.getEnd(), query.toString(), kind, value, detail, info);
	}

	public static SimpleProposal simpleProposal(IDocument doc, int offset, String query, CompletionItemKind kind, String value, String detail, Renderable info) {
		DocumentEdits edits = new DocumentEdits(doc, false);
		edits.replace(offset-query.length(), offset, value);
		return new SimpleProposal(edits, kind, info, detail, value);
	}
}
