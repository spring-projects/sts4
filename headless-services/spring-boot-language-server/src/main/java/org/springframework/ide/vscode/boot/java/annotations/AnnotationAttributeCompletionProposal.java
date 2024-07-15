/*******************************************************************************
 * Copyright (c) 2024 Broadcom
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.annotations;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ScoreableProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class AnnotationAttributeCompletionProposal extends ScoreableProposal {

	private static final String EMPTY_DETAIL = "";

	private final DocumentEdits edits;
	private final String label;
	private final String detail;
	private final Renderable documentation;
	private final double score;

	public AnnotationAttributeCompletionProposal(DocumentEdits edits, String label, String detail, Renderable documentation, double score) {
		this.edits = edits;
		this.label = label;
		this.detail = detail == null ? EMPTY_DETAIL : detail;
		this.documentation = documentation;
		this.score = score;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public CompletionItemKind getKind() {
		return CompletionItemKind.Value;
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
