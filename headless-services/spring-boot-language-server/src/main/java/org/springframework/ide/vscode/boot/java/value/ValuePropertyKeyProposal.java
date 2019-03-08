/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.value;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.springframework.ide.vscode.boot.common.InformationTemplates;
import org.springframework.ide.vscode.boot.metadata.PropertyInfo;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ScoreableProposal;
import org.springframework.ide.vscode.commons.util.FuzzyMap.Match;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * @author Martin Lippert
 */
public class ValuePropertyKeyProposal extends ScoreableProposal {

	private DocumentEdits edits;
	private String label;
	private String detail;
	private Renderable documentation;
	private double score;
	private InsertTextFormat textFormat;

	private ValuePropertyKeyProposal(DocumentEdits edits, String label, String detail, double score, Renderable documentation, InsertTextFormat textFormat) {
		this.edits = edits;
		this.label = label;
		this.detail = detail;
		this.documentation = documentation;
		this.score = score;
		this.textFormat = textFormat;
	}

	public ValuePropertyKeyProposal(DocumentEdits edits, Match<PropertyInfo> match, InsertTextFormat textFormat) {
		this(edits, match.data.getId(), match.data.getType(), match.score, InformationTemplates.createCompletionDocumentation(match.data), textFormat);
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public CompletionItemKind getKind() {
		return CompletionItemKind.Property;
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
		return score;
	}
	
	@Override
	public InsertTextFormat getInsertTextFormat() {
		return this.textFormat;
	}

}
