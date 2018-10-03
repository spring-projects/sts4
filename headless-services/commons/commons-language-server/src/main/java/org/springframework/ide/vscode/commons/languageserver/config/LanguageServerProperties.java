package org.springframework.ide.vscode.commons.languageserver.config;

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
}
