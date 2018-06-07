/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.completion;

import org.springframework.ide.vscode.commons.util.BadLocationException;

public interface IDocumentState {

	void insert(boolean grabCursor, int start, String text) throws BadLocationException;

	void delete(boolean grabCursor, int start, int end) throws BadLocationException;

}