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
package org.springframework.tooling.boot.ls.prefs;

import java.util.List;

import org.springsource.ide.eclipse.commons.livexp.ui.CommentSection;
import org.springsource.ide.eclipse.commons.livexp.ui.PreferencePageWithSections;
import org.springsource.ide.eclipse.commons.livexp.ui.PrefsPageSection;

import com.google.common.collect.ImmutableList;

public class RemoteAppsPrefsPage extends PreferencePageWithSections {

	public static final String PREF_REMOTE_BOOT_APPS_JSON = "boot-java.remote-apps";

	@Override
	protected List<PrefsPageSection> createSections() {
		return ImmutableList.of(
				new CommentSection(this, "For boot live hover support. Add jmxurl and hostname information for remote boot apps below. The format is as in this example:\n\n" +
						"[\n" +
						"   {\n" +
						"       \"jmxurl\" : \"service:jmx:rmi://localhost:44251/jndi/rmi://localhost:44251/jmxrmi\",\n" +
						"       \"host\" : \"my-remote-app.cfapps.io\"\n" +
						"       \"urlScheme\": \"http\", //optional, defaults to 'https'\n" +
						"       \"port\": 80 //optional, defaults to '443'\n" +
						"   }\n" +
						"]\n"
				),
				new RemoteAppsSection(this)
		);
	}
}


