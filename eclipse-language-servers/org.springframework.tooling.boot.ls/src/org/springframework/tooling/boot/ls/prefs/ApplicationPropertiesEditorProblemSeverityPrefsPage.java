/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.tooling.boot.ls.prefs;

import java.io.IOException;

import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferencesUtil;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferityPageFromMetadata;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

public class ApplicationPropertiesEditorProblemSeverityPrefsPage extends ProblemSeverityPreferityPageFromMetadata {
	
	public static final ProblemSeverityPreferencesUtil util = new ProblemSeverityPreferencesUtil("application.properties.problem.");
	
	public ApplicationPropertiesEditorProblemSeverityPrefsPage() throws IOException {
		super(util, LanguageServerProblemTypesMetadata.load().get("application-properties"));
	}

	@Override
	protected String getPluginId() {
		return BootLanguageServerPlugin.PLUGIN_ID;
	}

}
