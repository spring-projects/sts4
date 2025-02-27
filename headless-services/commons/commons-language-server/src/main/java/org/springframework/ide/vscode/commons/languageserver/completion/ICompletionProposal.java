/*******************************************************************************
 * Copyright (c) 2016, 2025 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.languageserver.completion;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.springframework.ide.vscode.commons.util.Renderable;

/**
 * Replaces STS/Eclipse's ICompletionProposal
 */
public interface ICompletionProposal {

	String getLabel();
	CompletionItemKind getKind();
	DocumentEdits getTextEdit();
	default Optional<Supplier<DocumentEdits>> getAdditionalEdit() { return Optional.empty(); }
	default boolean isTriggeringNextCompletionRequest() { return false; }

	String getDetail();
	Renderable getDocumentation();
	default String getFilterText() { return getLabel(); }
	
	/**
	 * Transforms a proposal to make it standout less somehow.
	 * @param howmuch A 'weight' for the deemphasis. Allowing to deempasize some proposals more than others.
	 */
	default ICompletionProposal deemphasize(double howmuch) { return this; }

	default boolean isDeprecated() { return false; }
	
	default TransformedCompletion dropLabelPrefix(int _numberOfDroppedChars) {
		String orgLabel = getLabel();
		int numberOfDroppedChars = Math.min(orgLabel.length(), _numberOfDroppedChars);
		String prefix = getLabel().substring(0, numberOfDroppedChars);
		return new TransformedCompletion(this) {
			@Override
			protected String tranformLabel(String originalLabel) {
				return originalLabel.substring(numberOfDroppedChars);
			}
			
			@Override
			protected DocumentEdits transformEdit(DocumentEdits textEdit) {
				textEdit.dropPrefix(prefix);
				return textEdit;
			}
		};
	}
	
	default Optional<Command> getCommand() {
		return Optional.empty();
	}
	
	default CompletionItemLabelDetails getLabelDetails() {
		return null;
	}
	
}
