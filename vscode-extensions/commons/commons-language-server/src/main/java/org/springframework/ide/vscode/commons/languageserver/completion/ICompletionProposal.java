package org.springframework.ide.vscode.commons.languageserver.completion;

import org.eclipse.lsp4j.CompletionItemKind;

/**
 * Replaces STS/Eclipse's ICompletionProposal
 */
public interface ICompletionProposal {

	/**
	 * Transforms a proposal to make it standout less somehow.
	 */
	ICompletionProposal deemphasize();

	String getLabel();
	CompletionItemKind getKind();
	DocumentEdits getTextEdit();

}
