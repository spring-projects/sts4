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

import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * Some 'custom' extensions to standard LSP {@link LanguageClient}.
 *
 * @author
 */
public interface STS4LanguageClient extends LanguageClient {

	@JsonNotification("sts/progress")
	void progress(ProgressParams progressEvent);

	/**
	 * The client/registerCapability request is sent from the server to the client
	 * to register for a new capability on the client side.
	 * Not all clients need to support dynamic capability registration.
	 * A client opts in via the ClientCapabilities.dynamicRegistration property
	 * <p>
	 * WARNING: This method doesn't formally exist in the LSP. It is actually
	 * called client/registerCapability. The reason we added it here is because
	 * of this: https://github.com/Microsoft/vscode-languageserver-node/issues/199
	 */
	@JsonRequest("client/registerFeature")
	CompletableFuture<Void> registerFeature(RegistrationParams params);


}
