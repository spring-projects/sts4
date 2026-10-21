/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(StandaloneFileWatcherProperties.PREFIX)
public class StandaloneFileWatcherProperties {

	public static final String PREFIX = "languageserver.standalone.filewatcher";

	private boolean enabled = true;
	private long debounceMs = 300;
	private int maxDirectories = 20000;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getDebounceMs() {
		return debounceMs;
	}

	public void setDebounceMs(long debounceMs) {
		this.debounceMs = debounceMs;
	}

	public int getMaxDirectories() {
		return maxDirectories;
	}

	public void setMaxDirectories(int maxDirectories) {
		this.maxDirectories = maxDirectories;
	}

}
