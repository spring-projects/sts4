/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.util;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.ServerCapabilities;

/**
 * {@link SimpleLanguageServer} will automatically find any {@link ServerCapabilityInitializer} beans
 * in the application context and call on them during language server initialization to allow
 * it to participate in initializing the ServerCapability the server returns to the client.
 */
public interface ServerCapabilityInitializer {
	void initialize(InitializeParams params, ServerCapabilities cap);
}
