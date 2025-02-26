/*******************************************************************************
 * Copyright (c) 2024, 2025 Broadcom
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
import org.springframework.ide.vscode.commons.languageserver.completion.AbstractScoreableProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class AnnotationAttributeCompletionProposal extends AbstractScoreableProposal {

	private final AnnotationAttributeProposal coreProposal;

	private final DocumentEdits edits;
	private final Renderable documentation;
	private final double score;

	public AnnotationAttributeCompletionProposal(DocumentEdits edits, AnnotationAttributeProposal coreProposal, Renderable documentation, double score) {
		this.edits = edits;
		this.coreProposal = coreProposal;
		this.documentation = documentation;
		this.score = score;
	}

	@Override
	public String getLabel() {
		return this.coreProposal.getLabel();
	}
	
	@Override
	public String getFilterText() {
		return this.coreProposal.getFilterText();
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
		return this.coreProposal.getDetail();
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
