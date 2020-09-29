/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.springframework.ide.eclipse.editor.support.util.HtmlSnippet;

public interface MissingLiveInfoMessages {

	static final String EXTERNAL_DOCUMENT_LINK = "https://github.com/spring-projects/sts4/wiki/Live-Application-Information#application-requirements-for-spring-boot-projects";

	static final MissingLiveInfoMessages DEFAULT = new MissingLiveInfoMessages() {};

	static final HtmlSnippet NOT_YET_COMPUTED = buffer -> buffer.p("Not yet computed...");

	default HtmlSnippet getMissingInfoMessage(String appName, String actuatorEndpoint) {

		return buffer -> {
			buffer.raw("<p>");
			buffer.raw("<b>");
			buffer.text(appName);
			buffer.raw("</b>");
			buffer.text(" must be running with JMX and actuator endpoint enabled:");
			buffer.raw("</p>");

			buffer.raw("<ol>");

			buffer.raw("<li>");
			buffer.text("Enable actuator ");
			buffer.raw("<b>");
			buffer.text(actuatorEndpoint);
			buffer.raw("</b>");
			buffer.text(" endpoint in the application.");
			buffer.raw("</li>");

			buffer.raw("<li>");
			buffer.text("Select ");
			buffer.raw("<b>");
			buffer.text("Enable JMX");
			buffer.raw("</b>");
			buffer.text(" in the application launch configuration.");
			buffer.raw("</li>");
			buffer.raw("</ol>");

			buffer.href(EXTERNAL_DOCUMENT_LINK, "See documentation");
		};

	}

	static HtmlSnippet noSelectionMessage(String element) {
		return buffer -> buffer.p("Select single element in Boot Dashboard to see live " + element);
	}

}
