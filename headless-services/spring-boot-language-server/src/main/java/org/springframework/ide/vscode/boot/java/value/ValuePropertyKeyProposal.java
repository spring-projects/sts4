/*******************************************************************************
 * Copyright (c) 2017, 2020 Pivotal, Inc.
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

	private static final String EMPTY_DETAIL = "";
	private DocumentEdits edits;
	private String label;
	private String detail;
	private Renderable documentation;
	private double score;

	private ValuePropertyKeyProposal(DocumentEdits edits, String label, String detail, double score, Renderable documentation) {
		this.edits = edits;
		this.label = label;
		// PT  161489998 - Detail for proposal must not be null. For some clients like Eclipse,
		// a null detail results in an NPE at JDT level when inserting the proposal in the editor, and results
		// in odd behaviour like insertion of an extra new line.
		this.detail = detail == null ? EMPTY_DETAIL : detail;
		this.documentation = documentation;
		this.score = score;
	}

	public ValuePropertyKeyProposal(DocumentEdits edits, Match<PropertyInfo> match) {
		this(edits, match.data.getId(), match.data.getType(), match.score, InformationTemplates.createCompletionDocumentation(match.data));
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

}
