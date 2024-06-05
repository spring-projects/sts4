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
package org.springframework.ide.vscode.boot.java.beans;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class QualifierCompletionProposal implements ICompletionProposal {

	private static final String EMPTY_DETAIL = "";

	private DocumentEdits edits;
	private String label;
	private String detail;
	private Renderable documentation;

	public QualifierCompletionProposal(DocumentEdits edits, String label, String detail, Renderable documentation) {
		this.edits = edits;
		this.label = label;
		// PT  161489998 - Detail for proposal must not be null. For some clients like Eclipse,
		// a null detail results in an NPE at JDT level when inserting the proposal in the editor, and results
		// in odd behaviour like insertion of an extra new line.
		this.detail = detail == null ? EMPTY_DETAIL : detail;
		this.documentation = documentation;
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

}
