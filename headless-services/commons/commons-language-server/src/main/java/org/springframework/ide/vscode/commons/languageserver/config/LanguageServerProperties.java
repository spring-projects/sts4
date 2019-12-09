/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.commons.languageserver.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("languageserver")
public class LanguageServerProperties {

	/**
	 * Enables 'standalone' launch mode. In standalone mode the language server
	 * creates a server socket and waits for a client to connect on that socket.
	 */
	private boolean standalone = false;

	/**
	 * The port on which a standalone language server listens. This setting
	 * is ignored if the server is not launched in standalone mode.
	 */
	private int standalonePort = 5007;

	/**
	 * Extension id is a unique identifier associated with a type of language server.
	 * It is used to derive further unique ids that should be 'scoped' to a particular
	 * language server. For example ids for vscode comands used to define code actions
	 * are derived by appending command names with this id.
	 */
	private String extensionId;

	/**
	 * List of characters that trigger completions.
	 */
	private Map<String, String> completionTriggerCharacters;
	
	/**
	 * Hover request handler timeout in milliseconds
	 */
	private long hoverTimeout = -1;

	public boolean isStandalone() {
		return standalone;
	}

	public void setStandalone(boolean standalone) {
		this.standalone = standalone;
	}

	public int getStandalonePort() {
		return standalonePort;
	}

	public void setStandalonePort(int standalonePort) {
		this.standalonePort = standalonePort;
	}

	public String getExtensionId() {
		return extensionId;
	}

	public void setExtensionId(String extensionId) {
		this.extensionId = extensionId;
	}

	public Map<String,String> getCompletionTriggerCharacters() {
		return completionTriggerCharacters;
	}

	public void setCompletionTriggerCharacters(Map<String, String> completionTriggerCharacters) {
		this.completionTriggerCharacters = completionTriggerCharacters;
	}

	public long getHoverTimeout() {
		return hoverTimeout;
	}

	public void setHoverTimeout(long hoverTimeout) {
		this.hoverTimeout = hoverTimeout;
	}

}
