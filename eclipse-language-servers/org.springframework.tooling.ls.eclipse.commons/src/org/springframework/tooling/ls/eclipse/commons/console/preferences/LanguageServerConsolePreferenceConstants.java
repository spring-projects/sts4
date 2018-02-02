/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.console.preferences;

public class LanguageServerConsolePreferenceConstants {
	
	public static final boolean ENABLE_BY_DEFAULT = true;

	public static final String PREF_BOOT_JAVA_CONSOLE_ENABLED = "boot-java.console.enabled";
	public static final String PREF_BOOT_PROPS_CONSOLE_ENABLED = "boot-properties.console.enabled";
	public static final String PREF_CONCOURSE_CONSOLE_ENABLED = "concourse.console.enabled";
	public static final String PREF_CLOUDFOUNDRY_CONSOLE_ENABLED = "cloudfoundry.console.enabled";
	public static final String PREF_BOSH_CONSOLE_ENABLED = "bosh.console.enabled";

	public static final ServerInfo BOOT_JAVA_SERVER = new ServerInfo(PREF_BOOT_JAVA_CONSOLE_ENABLED, "Boot Java");
	public static final ServerInfo BOOT_PROPS_SERVER = new ServerInfo(PREF_BOOT_PROPS_CONSOLE_ENABLED, "Boot Properties");
	public static final ServerInfo CLOUDFOUNDRY_SERVER = new ServerInfo(PREF_CLOUDFOUNDRY_CONSOLE_ENABLED, "Cloudfoundry");
	public static final ServerInfo CONCOURSE_SERVER = new ServerInfo(PREF_CONCOURSE_CONSOLE_ENABLED, "Concourse");
	public static final ServerInfo BOSH_SERVER = new ServerInfo(PREF_BOSH_CONSOLE_ENABLED, "Bosh");
	
	public static final ServerInfo[] ALL_SERVERS = {
			BOOT_JAVA_SERVER,
			BOOT_PROPS_SERVER,
			CLOUDFOUNDRY_SERVER,
			CONCOURSE_SERVER,
			BOSH_SERVER
	};

	public static class ServerInfo {
		public final String preferenceKey;
		public final String label;
		public ServerInfo(String preferenceKey, String label) {
			super();
			this.preferenceKey = preferenceKey;
			this.label = label;
		}
		@Override
		public String toString() {
			return "ServerInfo [preferenceKey=" + preferenceKey + ", label=" + label + "]";
		}
	}
}
