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

package org.springframework.ide.vscode.commons.languageserver.completion;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.util.Renderable;

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

	String getDetail();
	Renderable getDocumentation();
	default String getFilterText() { return getLabel(); }

}
