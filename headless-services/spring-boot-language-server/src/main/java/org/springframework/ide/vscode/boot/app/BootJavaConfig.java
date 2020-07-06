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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
	
	public static final boolean LIVE_INFORMATION_AUTOMATIC_TRACKING_ENABLED_DEFAULT = true;
	public static final int LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY_DEFAULT = 5000;
	
	public static final int LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO_DEFAULT = 10;
	public static final int LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS_DEFAULT = 3;
	
	public static final boolean VALIDAITON_SPEL_EXPRESSIONS_ENABLED_DEFAULT = true;


	//TODO: Consider changing this to something that raises Spring application events.
	// I.e. like described in here: https://www.baeldung.com/spring-events

	private final SimpleWorkspaceService workspace;
	private Settings settings = new Settings(null);
	private ListenerList<Void> listeners = new ListenerList<Void>();

	BootJavaConfig(SimpleLanguageServer server) {
		this.workspace = server.getWorkspaceService();
	}

	public boolean isLiveInformationAutomaticTrackingEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "live-information", "automatic-tracking", "on");
		return enabled != null ? enabled.booleanValue() : LIVE_INFORMATION_AUTOMATIC_TRACKING_ENABLED_DEFAULT;
	}

	public int getLiveInformationAutomaticTrackingDelay() {
		Integer delay = settings.getInt("boot-java", "live-information", "automatic-tracking", "delay");
		return delay != null ? delay.intValue() : LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY_DEFAULT;
	}

	public int getLiveInformationFetchDataMaxRetryCount() {
		Integer delay = settings.getInt("boot-java", "live-information", "fetch-data", "max-retries");
		return delay != null ? delay.intValue() : LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO_DEFAULT;
	}

	public int getLiveInformationFetchDataRetryDelayInSeconds() {
		Integer delay = settings.getInt("boot-java", "live-information", "fetch-data", "retry-delay-in-seconds");
		return delay != null ? delay.intValue() : LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS_DEFAULT;
	}

	public boolean isSpringXMLSupportEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "support-spring-xml-config", "on");
		return enabled != null && enabled.booleanValue();
	}
	
	public boolean isScanJavaTestSourcesEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "scan-java-test-sources", "on");
		return enabled != null && enabled.booleanValue();
	}
	
	public String[] xmlBeansFoldersToScan() {
		String foldersStr = settings.getString("boot-java", "support-spring-xml-config", "scan-folders");
		if (foldersStr != null) {
			foldersStr = foldersStr.trim();
		}
		String[] folders = foldersStr == null || foldersStr.isEmpty()? new String[0] : foldersStr.split("\\s*,\\s*");
		List<String> cleanedFolders = new ArrayList<>(folders.length);
		for (String folder : folders) {
			int startIndex = 0;
			int endIndex = folder.length();
			if (folder.startsWith(File.separator)) {
				startIndex += File.separator.length();
			}
			if (folder.endsWith(File.separator)) {
				endIndex -= File.separator.length();
			}
			if (startIndex > 0 || endIndex < folder.length()) {
				if (startIndex < endIndex) {
					cleanedFolders.add(folder.substring(startIndex, endIndex));
				}
			} else {
				cleanedFolders.add(folder);
			}
		}
		return cleanedFolders.toArray(new String[cleanedFolders.size()]);
	}

	public boolean isChangeDetectionEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "change-detection", "on");
		return enabled != null && enabled.booleanValue();
	}

	public boolean isSpelExpressionValidationEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "validation", "spel", "on");
		return enabled != null ? enabled.booleanValue() : VALIDAITON_SPEL_EXPRESSIONS_ENABLED_DEFAULT;
	}

	public boolean areXmlHyperlinksEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "support-spring-xml-config", "hyperlinks");
		return enabled != null && enabled.booleanValue();
	}
	
	public boolean isXmlContentAssistEnabled() {
		Boolean enabled = settings.getBoolean("boot-java", "support-spring-xml-config", "content-assist");
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
