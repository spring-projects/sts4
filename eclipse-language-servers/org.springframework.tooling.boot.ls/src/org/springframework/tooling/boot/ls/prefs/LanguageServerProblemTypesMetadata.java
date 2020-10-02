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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.springframework.ide.eclipse.editor.support.preferences.ProblemSeverityPreferityPageFromMetadata.ProblemTypeData;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

public class LanguageServerProblemTypesMetadata {

	public static Map<String, ProblemTypeData[]> load() throws IOException {
		File root = FileLocator.getBundleFile(BootLanguageServerPlugin.getDefault().getBundle());
		File metadataFile = root.toPath().resolve("servers/spring-boot-language-server/BOOT-INF/classes/problem-types.json").toFile();
		return ApplicationPropertiesEditorProblemSeverityPrefsPage.readFromFile(metadataFile);
	}

}
