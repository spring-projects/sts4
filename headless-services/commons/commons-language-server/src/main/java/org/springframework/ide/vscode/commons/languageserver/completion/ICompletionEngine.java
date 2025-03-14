/*******************************************************************************
 * Copyright (c) 2015, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

/**
 * @author Kris De Volder
 */
public interface ICompletionEngine {

	InternalCompletionList getCompletions(TextDocument document, int offset) throws Exception;

	default boolean keepCompletionsOrder(IDocument doc) { return false; };

}
