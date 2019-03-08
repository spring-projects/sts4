/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.yaml.schema;

import java.util.List;

import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.yaml.completion.CompletionFactory;

/**
 * Interface that can be used by a {@link ICompletionEngine} to delegate to a
 * 'helper' completion engine that computes completions for some sub-region of
 * the document. 
 * 
 * @author Kris De Volder
 */
@FunctionalInterface
public interface ISubCompletionEngine {
	
	/**
	 * @param region The relevant subregion of the document (typically, the sub-engine only cares about the text
	 *         within this particular region.)
	 * @param offset The cursor position relative to the region.
	 */
	List<ICompletionProposal> getCompletions(CompletionFactory f, DocumentRegion region, int offset);


}
