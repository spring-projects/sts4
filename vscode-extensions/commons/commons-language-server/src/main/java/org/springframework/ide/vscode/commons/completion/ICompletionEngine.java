/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.completion;

import java.util.Collection;

import org.springframework.ide.vscode.util.IDocument;

/**
 * @author Kris De Volder
 */
public interface ICompletionEngine {

	Collection<ICompletionProposal> getCompletions(IDocument document, int offset) throws Exception;

}
