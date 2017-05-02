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

package org.springframework.ide.vscode.commons.languageserver;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixEdit.CursorMovement;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/progress")
	void progress(ProgressParams progressEvent);

	@JsonRequest("sts/moveCursor")
	CompletableFuture<Object> moveCursor(CursorMovement cursorMovement);

}
