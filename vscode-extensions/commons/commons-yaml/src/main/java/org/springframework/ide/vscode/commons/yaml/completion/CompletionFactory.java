/*******************************************************************************
 * Copyright (c) 2016-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.vscode.commons.yaml.completion;

import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.yaml.schema.YType;
import org.springframework.ide.vscode.commons.yaml.schema.YTypeUtil;
import org.springframework.ide.vscode.commons.yaml.schema.YTypedProperty;

public interface CompletionFactory {

	CompletionFactory DEFAULT = new DefaultCompletionFactory();

	ICompletionProposal beanProperty(IDocument doc, String contextProperty, YType contextType, String query, YTypedProperty p, double score, DocumentEdits edits, YTypeUtil typeUtil);
	ICompletionProposal valueProposal(String value, String query, String label, YType type, double score, DocumentEdits edits, YTypeUtil typeUtil);
    
	/**
	 * Creates a completion with an EMPTY value and a display message indicating some error condition. The purpose of this is
	 * to show the user a completion with some meaningful information on why completion values were not fetched.
	 * 
	 * @param message the error message to display to the user in the completion
	 * @param query
	 * @param type
	 * @param edits
	 * @param typeUtil
	 * @return
	 */
	ICompletionProposal errorMessage(String message, String query, YType type, DocumentEdits edits, YTypeUtil typeUtil);
}
