/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.quickfix;

import org.eclipse.lsp4j.WorkspaceEdit;
import org.springframework.ide.vscode.commons.protocol.CursorMovement;

public class QuickfixEdit {

	public final WorkspaceEdit workspaceEdit;
	public final CursorMovement cursorMovement;

	public QuickfixEdit(WorkspaceEdit workspaceEdit, CursorMovement cursorMovement) {
		this.workspaceEdit = workspaceEdit;
		this.cursorMovement = cursorMovement;
	}
}
