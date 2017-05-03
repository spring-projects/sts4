/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.quickfix;

import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.WorkspaceEdit;

public class QuickfixEdit {

	public static class CursorMovement {
		private String uri;
		private Position position;

		public CursorMovement() {
		}

		public CursorMovement(String uri, Position position) {
			this.uri = uri;
			this.position = position;
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public Position getPosition() {
			return position;
		}

		public void setPosition(Position position) {
			this.position = position;
		}

	}

	public final WorkspaceEdit workspaceEdit;
	public final CursorMovement cursorMovement;

	public QuickfixEdit(WorkspaceEdit workspaceEdit, CursorMovement cursorMovement) {
		this.workspaceEdit = workspaceEdit;
		this.cursorMovement = cursorMovement;
	}
}
