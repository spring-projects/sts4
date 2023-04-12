/*******************************************************************************
 * Copyright (c) 2018, 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.ls.eclipse.commons.preferences;

public class LanguageServerConsolePreferenceConstants {

	public static final boolean ENABLE_BY_DEFAULT = false;

	public static final String PREF_BOOT_JAVA_CONSOLE_ENABLED = "boot-java.console.enabled";
	public static final String PREF_BOOT_JAVA_LOG_FILE = "boot-java.log.file";
	public static final String PREF_CONCOURSE_CONSOLE_ENABLED = "concourse.console.enabled";
	public static final String PREF_CONCOURSE_FILE_LOG = "concourse.file.log";
	public static final String PREF_CLOUDFOUNDRY_CONSOLE_ENABLED = "cloudfoundry.console.enabled";
	public static final String PREF_CLOUDFOUNDRY_LOG_FILE = "cloudfoundry.log.file";
	public static final String PREF_BOSH_CONSOLE_ENABLED = "bosh.console.enabled";
	public static final String PREF_BOSH_LOG_FILE = "bosh.log.file";

	public static final ServerInfo SPRING_BOOT_SERVER = new ServerInfo(PREF_BOOT_JAVA_CONSOLE_ENABLED, PREF_BOOT_JAVA_LOG_FILE, "Spring Boot", "org.springframework.tooling.boot.ls");
	public static final ServerInfo CLOUDFOUNDRY_SERVER = new ServerInfo(PREF_CLOUDFOUNDRY_CONSOLE_ENABLED, PREF_CLOUDFOUNDRY_LOG_FILE, "Cloudfoundry", "org.springframework.tooling.cloudfoundry.manifest.ls");
	public static final ServerInfo CONCOURSE_SERVER = new ServerInfo(PREF_CONCOURSE_CONSOLE_ENABLED, PREF_CONCOURSE_FILE_LOG, "Concourse", "org.springframework.tooling.concourse.ls");
	public static final ServerInfo BOSH_SERVER = new ServerInfo(PREF_BOSH_CONSOLE_ENABLED, PREF_BOSH_LOG_FILE, "Bosh", "org.springframework.tooling.bosh.ls");

	public static final ServerInfo[] ALL_SERVERS = {
			SPRING_BOOT_SERVER,
			CLOUDFOUNDRY_SERVER,
			CONCOURSE_SERVER,
			BOSH_SERVER
	};

	public static class ServerInfo {
		public final String preferenceKeyConsoleLog;
		public final String preferenceKeyFileLog;
		public final String label;
		public final String bundleId;
		public ServerInfo(String preferenceKeyConsoleLog, String preferenceKeyFileLog, String label, String bundleId) {
			this.preferenceKeyConsoleLog = preferenceKeyConsoleLog;
			this.preferenceKeyFileLog = preferenceKeyFileLog;
			this.label = label;
			this.bundleId = bundleId;
		}
		@Override
		public String toString() {
			return "ServerInfo [preferenceKeyConsoleLog=" + preferenceKeyConsoleLog + ", label=" + label + ", bundleId=" + bundleId + "]";
		}
	}
}
