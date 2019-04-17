/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data;

import java.util.Optional;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

public class FindByCompletionProposal implements ICompletionProposal {

	private String label;
	private CompletionItemKind kind;
	private DocumentEdits edits;
	private String details;
	private Renderable doc;
	private Optional<DocumentEdits> additionalEdits;
	private String filter;

	public FindByCompletionProposal(String label, CompletionItemKind kind, DocumentEdits edits, String details,
			Renderable doc, Optional<DocumentEdits> additionalEdits, String filter) {
		super();
		this.label = label;
		this.kind = kind;
		this.edits = edits;
		this.details = details;
		this.doc = doc;
		this.additionalEdits = additionalEdits;
		this.filter = filter;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public CompletionItemKind getKind() {
		return kind;
	}

	@Override
	public DocumentEdits getTextEdit() {
		return edits;
	}

	@Override
	public String getDetail() {
		return details;
	}

	@Override
	public Renderable getDocumentation() {
		return doc;
	}

	@Override
	public Optional<DocumentEdits> getAdditionalEdit() {
		return additionalEdits;
	}

	@Override
	public String getFilterText() {
		return filter;
	}

}
