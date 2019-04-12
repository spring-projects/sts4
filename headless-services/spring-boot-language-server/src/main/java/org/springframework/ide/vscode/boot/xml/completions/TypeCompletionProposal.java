/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ScoreableProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class TypeCompletionProposal extends ScoreableProposal {

	private final String label;
	private final CompletionItemKind kind;
	private final DocumentEdits edits;
	private final String detail;
	private final Renderable documentation;
	private final double score;

	public TypeCompletionProposal(String label, CompletionItemKind kind, DocumentEdits edits, String detail, Renderable documentation, double score) {
		super();
		this.label = label;
		this.kind = kind;
		this.edits = edits;
		this.detail = detail;
		this.documentation = documentation;
		this.score = score;
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

}
