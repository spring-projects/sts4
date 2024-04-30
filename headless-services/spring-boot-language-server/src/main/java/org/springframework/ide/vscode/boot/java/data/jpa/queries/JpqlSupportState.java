/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public final class JpqlSupportState {

	private final SimpleLanguageServer server;
	private boolean enabled;

	public JpqlSupportState(SimpleLanguageServer server, ProjectObserver projectObserver, BootJavaConfig config) {
		this(server, projectObserver, config, config.isJpqlEnabled());
	}

	public JpqlSupportState(SimpleLanguageServer server, ProjectObserver projectObserver, BootJavaConfig config, boolean enabled) {
		this.server = server;
		this.enabled = enabled;
		config.addListener(v -> setEnabled(config.isJpqlEnabled()));
		projectObserver.addListener(ProjectObserver.onAny(jp -> server.getAsync().execute(() -> server.getClient().refreshSemanticTokens())));
	}
	
	public synchronized boolean isEnabled() {
		return enabled;
	}
	
	private synchronized void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			this.enabled = enabled;
			if (server.getWorkspaceService().supportsSemanticTokensRefresh()) {
				server.getAsync().execute(() -> server.getClient().refreshSemanticTokens());
			}
		}
	}
}
