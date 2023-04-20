/*******************************************************************************
 * Copyright (c) 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.providers;

import java.util.Collection;

import org.eclipse.jdt.core.dom.ASTNode;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryDefinition;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * Responsible for creating proposals related to Spring Data repositories.
 * @author danthe1st
 */
public interface DataRepositoryCompletionProvider {

	void addProposals(Collection<ICompletionProposal> completions, IDocument doc, int offset, String prefix, DataRepositoryDefinition repo, ASTNode node);

}
