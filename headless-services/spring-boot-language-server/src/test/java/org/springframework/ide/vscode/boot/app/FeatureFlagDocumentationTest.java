/*******************************************************************************
 * Copyright (c) 2026 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Keeps the places that list the feature ids in sync with {@link FeatureFlag}: the description of the
 * setting in the {@code package.json} of the VS Code extension and {@code FEATURE_FLAGS.md}.
 *
 * @author Martin Lippert
 */
public class FeatureFlagDocumentationTest {

	private static final Path FEATURE_FLAGS_MD = Paths.get("FEATURE_FLAGS.md");
	private static final Path PACKAGE_JSON = Paths.get("../../vscode-extensions/vscode-spring-boot/package.json");

	private static final Pattern BACKTICKED_ID = Pattern.compile("`([a-z0-9]+(?:-[a-z0-9]+)+)`");
	private static final Pattern TABLE_ROW_ID = Pattern.compile("(?m)^\\| `([a-z0-9-]+)` \\|");

	private static Set<String> featureIds() {
		return Arrays.stream(FeatureFlag.values()).map(FeatureFlag::getId).collect(Collectors.toCollection(TreeSet::new));
	}

	private static Set<String> matches(Pattern pattern, String text) {
		Set<String> ids = new TreeSet<>();
		Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			ids.add(matcher.group(1));
		}
		return ids;
	}

	@Test
	void packageJsonListsAllIds() throws Exception {
		assumeTrue(Files.isRegularFile(PACKAGE_JSON), "VS Code extension not found next to the language server");

		JsonObject packageJson = JsonParser.parseString(Files.readString(PACKAGE_JSON)).getAsJsonObject();
		String description = null;
		for (var configuration : packageJson.getAsJsonObject("contributes").getAsJsonArray("configuration")) {
			JsonObject properties = configuration.getAsJsonObject().getAsJsonObject("properties");
			if (properties != null && properties.has("boot-java.feature-flags")) {
				description = properties.getAsJsonObject("boot-java.feature-flags").get("markdownDescription").getAsString();
			}
		}

		assertEquals(featureIds(), matches(BACKTICKED_ID, description));
	}

	@Test
	void documentationListsAllIds() throws Exception {
		assertEquals(featureIds(), matches(TABLE_ROW_ID, Files.readString(FEATURE_FLAGS_MD)));
	}

}
