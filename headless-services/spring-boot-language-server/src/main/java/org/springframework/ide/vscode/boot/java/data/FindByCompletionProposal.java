/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
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
import java.util.function.Supplier;

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
	private Supplier<DocumentEdits> additionalEdits;
	private String filter;
	private boolean triggerNextCompletion;

	public FindByCompletionProposal(String label, CompletionItemKind kind, DocumentEdits edits, String details,
			Renderable doc, Supplier<DocumentEdits> additionalEdits, String filter, boolean triggerNextCompletion) {
		super();
		this.label = label;
		this.kind = kind;
		this.edits = edits;
		this.details = details;
		this.doc = doc;
		this.additionalEdits = additionalEdits;
		this.filter = filter;
		this.triggerNextCompletion = triggerNextCompletion;
	}

	public static ICompletionProposal createProposal(int offset, CompletionItemKind completionItemKind, String prefix, String label, String completion, boolean triggerNextCompletion, Supplier<DocumentEdits> additionalEdits) {
		DocumentEdits edits = new DocumentEdits(null, false);
		String filter = label;
		if (prefix != null && label.startsWith(prefix)) {
			edits.replace(offset - prefix.length(), offset, completion);
		}
		else if (prefix != null && completion.startsWith(prefix)) {
			edits.replace(offset - prefix.length(), offset, completion);
			filter = completion;
		}
		else {
			edits.insert(offset, completion);
		}

		return new FindByCompletionProposal(label, completionItemKind, edits, null, null, additionalEdits, filter, triggerNextCompletion);
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
	public Optional<Supplier<DocumentEdits>> getAdditionalEdit() {
		return Optional.ofNullable(additionalEdits);
	}

	@Override
	public String getFilterText() {
		return filter;
	}

	@Override
	public boolean isTriggeringNextCompletionRequest() {
		return triggerNextCompletion;
	}

}
