/*******************************************************************************
 * Copyright (c) 2019, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.AbstractScoreableProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class GenericXMLCompletionProposal extends AbstractScoreableProposal {

	private final String label;
	private final CompletionItemKind kind;
	private final DocumentEdits edits;
	private final String detail;
	private final Renderable documentation;
	private final double score;
	private final String filterText;
	private final DocumentEdits additionalEdits;

	public GenericXMLCompletionProposal(String label, CompletionItemKind kind, DocumentEdits edits, String detail, Renderable documentation, double score) {
		this(label, kind, edits, detail, documentation, score, label, null);
	}

	public GenericXMLCompletionProposal(String label, CompletionItemKind kind, DocumentEdits edits, String detail, Renderable documentation, double score, String filterText, DocumentEdits additionalEdits) {
		super();
		this.label = label;
		this.kind = kind;
		this.edits = edits;
		this.detail = detail;
		this.documentation = documentation;
		this.score = score;
		this.filterText = filterText;
		this.additionalEdits = additionalEdits;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public CompletionItemKind getKind() {
		return this.kind;
	}

	@Override
	public DocumentEdits getTextEdit() {
		return this.edits;
	}

	@Override
	public String getDetail() {
		return this.detail;
	}

	@Override
	public Renderable getDocumentation() {
		return this.documentation;
	}

	@Override
	public double getBaseScore() {
		return this.score;
	}
	
	@Override
	public String getFilterText() {
		return this.filterText;
	}
	
	@Override
	public Optional<Supplier<DocumentEdits>> getAdditionalEdit() {
		if (this.additionalEdits != null) {
			return Optional.of(() -> additionalEdits);
		}
		else {
			return Optional.empty();
		}
	}

}
