/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.bosh;

import java.time.Duration;

import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.util.Log;
import org.springframework.stereotype.Component;

/**
 * Provides access to configuration options that allow user to
 * change the way bosh CLI commands are executed by the
 * bosh language server.
 *
 * @author Kris De Volder
 */
@Component
public class BoshCliConfig {

	/**
	 * The settings object. This is obtained from 'didChangeConfiguration' events.
	 */
	private Settings settings = new Settings(null);

	public String getCommand() {
		return settings.getString("bosh", "cli", "command");
	}
	public String getTarget() {
		return settings.getString("bosh", "cli", "target");
	}
	public Duration getTimeout() {
		Integer seconds = settings.getInt("cli", "timeout");
		return seconds == null ? Duration.ofSeconds(3) : Duration.ofSeconds(seconds);
	}

	public void handleConfigurationChange(Settings newConfig) {
		Log.info("Settings received: "+newConfig);
		this.settings = newConfig;
	}
}
