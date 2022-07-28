/*******************************************************************************
 * Copyright (c) 2020, 2022 Pivotal, Inc.
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.springframework.tooling.boot.ls.BootLanguageServerPlugin;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

public class LanguageServerProblemTypesMetadata {

	public static List<ProblemCategoryData> load() throws IOException {
		File root = FileLocator.getBundleFile(BootLanguageServerPlugin.getDefault().getBundle());
		File metadataFile = root.toPath().resolve("servers/spring-boot-language-server/BOOT-INF/classes/problem-types.json").toFile();
		return readCategoriesFromFile(metadataFile);
	}
	
	public static List<ProblemCategoryData> readCategoriesFromFile(File metadataFile) throws FileNotFoundException, IOException {
		Gson gson = new Gson();
		TypeToken<List<ProblemCategoryData>> tt = new TypeToken<List<ProblemCategoryData>>() {
			private static final long serialVersionUID = 1L;
		};
		try (Reader json = new FileReader(metadataFile)) {
			return gson.fromJson(json, tt.getType());
		}
	}


}
