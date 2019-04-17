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
package org.springframework.ide.vscode.boot.java.scope;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class ScopeNameCompletionProposal implements ICompletionProposal {

	public static final ScopeNameCompletion[] COMPLETIONS = new ScopeNameCompletion[] {
			new ScopeNameCompletion("\"prototype\"", "prototype", "prototype scope", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"singleton\"", "singleton", "singleton scope (default)", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"request\"", "request", "request scope", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"session\"", "session", "session scope", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"globalSession\"", "globalSession", "globalSession scope", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"application\"", "application", "application scope", null, CompletionItemKind.Value),
			new ScopeNameCompletion("\"websocket\"", "websocket", "websocket scope", null, CompletionItemKind.Value)
	};

	private final IDocument doc;
	private final int startOffset;
	private final int endOffset;
	private final ScopeNameCompletion completion;
	private final String prefix;

	public ScopeNameCompletionProposal(ScopeNameCompletion completion, IDocument doc, int startOffset, int endOffset, String prefix) {
		this.completion = completion;
		this.doc = doc;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.prefix = prefix;
	}

	@Override
	public String getLabel() {
		return completion.getLabel();
	}

	@Override
	public CompletionItemKind getKind() {
		return completion.getKind();
	}

	@Override
	public DocumentEdits getTextEdit() {
		DocumentEdits edits = new DocumentEdits(doc, false);
		edits.replace(startOffset + prefix.length(), endOffset, completion.getValue().substring(prefix.length()));
		return edits;
	}

	@Override
	public String getDetail() {
		return completion.getDetail();
	}

	@Override
	public Renderable getDocumentation() {
		return completion.getDocumentation();
	}

}
