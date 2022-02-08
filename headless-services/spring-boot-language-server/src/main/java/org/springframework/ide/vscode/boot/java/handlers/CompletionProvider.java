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
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.Collection;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public interface CompletionProvider {

	void provideCompletions(J node, Annotation annotation, int offset, IDocument doc, Collection<ICompletionProposal> completions);
	void provideCompletions(J node, int offset, IDocument doc, Collection<ICompletionProposal> completions);

}
