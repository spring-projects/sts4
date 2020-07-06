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
package org.springframework.tooling.boot.ls;

/**
 * @author Martin Lippert
 */
public class Constants {
	
	public static final String PLUGIN_ID = "org.springframework.tooling.boot.ls";
	
	public static final String PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_ENABLED = "boot-java.live-information.automatic-tracking.on";
	public static final String PREF_LIVE_INFORMATION_AUTOMATIC_TRACKING_DELAY = "boot-java.live-information.automatic-tracking.delay";

	public static final String PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_MAX_NO = "boot-java.live-information.fetch-data.max-retries";
	public static final String PREF_LIVE_INFORMATION_FETCH_DATA_RETRY_DELAY_IN_SECONDS = "boot-java.live-information.fetch-data.retry-delay-in-seconds";

	public static final String PREF_SCAN_JAVA_TEST_SOURCES = "boot-java.scan-java-test-sources";

	public static final String PREF_VALIDATION_SPEL_EXPRESSIONS = "boot-java.validation.spel.on";

	public static final String PREF_SUPPORT_SPRING_XML_CONFIGS = "boot-java.support-spring-xml-config.on";
	public static final String PREF_XML_CONFIGS_SCAN_FOLDERS = "boot-java.support-spring-xml-config.scan-folders-globs";
	public static final String PREF_XML_CONFIGS_HYPERLINKS = "boot-java.support-spring-xml-config.hyperlinks";
	public static final String PREF_XML_CONFIGS_CONTENT_ASSIST = "boot-java.support-spring-xml-config.content-assist";

	public static final String PREF_CHANGE_DETECTION = "boot-java.change-detection.on";

}
