package org.springframework.ide.vscode.commons.completion;

import io.typefox.lsapi.CompletionItemKind;

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
