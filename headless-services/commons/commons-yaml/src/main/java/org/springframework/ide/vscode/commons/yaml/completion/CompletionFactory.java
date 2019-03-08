/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.completion;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;

public interface CompletionFactory {

	CompletionFactory DEFAULT = new DefaultCompletionFactory();

	ICompletionProposal beanProperty(
			IDocument doc, String contextProperty, YType contextType, String query,
			YTypedProperty p, double score, DocumentEdits edits, YTypeUtil typeUtil
	);
	ICompletionProposal valueProposal(
			String value, String query, String label, YType type, Renderable doc,
			double score, DocumentEdits edits, YTypeUtil typeUtil
	);

	/**
	 * Create a fake completion proposal that represents an error message. Such a proposal, when applied does nothing. Its main purpose is to
	 * show a (possibly lengthy) error message to the user.
	 * <p>
	 * If the error message is long you can include a ": " to divide the string into a 'short message' and a longer explanation. The longer explanation
	 * will be chopped-off from the message and displayed as a 'doc string'.
	 */
	ICompletionProposal errorMessage(String query, String message);
}
