/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import java.util.function.Consumer;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.ide.vscode.commons.languageserver.util.ListenerList;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleWorkspaceService;
import org.springframework.stereotype.Component;

/**
 * Boot-Java LS settings
 *
 * @author Alex Boyko
 */
@Component
public class BootJavaConfig implements InitializingBean {

	//TODO: Consider changing this to something that raises Spring application events.
	// I.e. like described in here: https://www.baeldung.com/spring-events

	private final SimpleWorkspaceService workspace;
	private Settings settings = new Settings(null);
	private ListenerList<Void> listeners = new ListenerList<Void>();

	BootJavaConfig(SimpleLanguageServer server) {
		this.workspace = server.getWorkspaceService();
	}


	public boolean isBootHintsEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "boot-hints", "on");
		return enabled == null || enabled.booleanValue();
	}

	public boolean isSpringXMLSupportEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "support-spring-xml-config", "on");
		return enabled != null && enabled.booleanValue();
	}

	public boolean isChangeDetectionEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "change-detection", "on");
		return enabled != null && enabled.booleanValue();
	}

	public void handleConfigurationChange(Settings newConfig) {
		this.settings = newConfig;
		listeners.fire(null);
	}

	public void addListener(Consumer<Void> l) {
		listeners.add(l);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		workspace.onDidChangeConfiguraton(this::handleConfigurationChange);
	}
}
