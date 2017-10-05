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
package org.springframework.tooling.ls.eclipse.commons;

import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author Kris De Volder
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/highlight")
	void highlight(HighlightParams highlights);

// TODO: @JsonNotification("sts/progress")
//	void progress(ProgressParams progressEvent);
//
// TODO: @JsonRequest("sts/moveCursor")
//	CompletableFuture<Object> moveCursor(CursorMovement cursorMovement);

}
