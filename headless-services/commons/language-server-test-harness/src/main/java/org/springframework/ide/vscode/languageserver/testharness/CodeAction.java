/*******************************************************************************
 * Copyright (c) 2017, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.languageserver.testharness;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

/**
 * Wrapper for the test harness to refer to and manipulate a
 * CodeAction.
 */
public class CodeAction {

	public final Either<Command, org.eclipse.lsp4j.CodeAction> ca;
	private LanguageServerHarness harness;

	public CodeAction(LanguageServerHarness harness, Either<Command, org.eclipse.lsp4j.CodeAction> ca) {
		super();
		this.harness = harness;

		this.ca = ca;
	}

	@Override
	public String toString() {
		return ca.toString();
	}

	public String getLabel() {
		return ca.isLeft() ? ca.getLeft().getTitle() : ca.getRight().getTitle();
	}

	public void perform() throws Exception {
		if (ca.isLeft()) {
			harness.perform(ca.getLeft());
		} else {
			org.eclipse.lsp4j.CodeAction codeAction = ca.getRight();
			if (codeAction.getCommand() != null) {
				harness.perform(codeAction.getCommand());
			} else if (codeAction.getEdit() != null) {
				// apply workspace edit
				throw new UnsupportedOperationException("CodeAction workspaceEdit not yet supported");
			} else {				
				throw new UnsupportedOperationException("resolve/codeAction not yet supported");
			}
		}
	}
	
	public Command getCommand() {
		return ca.isLeft() ? ca.getLeft() : ca.getRight().getCommand();
	}

}
