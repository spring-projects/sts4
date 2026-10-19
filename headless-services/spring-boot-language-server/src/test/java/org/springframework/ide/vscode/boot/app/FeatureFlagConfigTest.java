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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.languageserver.util.Settings;

import com.google.gson.JsonParser;

/**
 * @author Martin Lippert
 */
public class FeatureFlagConfigTest {

	private BootJavaConfig configWith(String json) {
		BootJavaConfig config = new BootJavaConfig(null);
		config.handleConfigurationChange(new Settings(json == null ? null : JsonParser.parseString(json)));
		return config;
	}

	@Test
	void allFeaturesEnabledWithoutSettings() {
		BootJavaConfig config = configWith(null);
		for (FeatureFlag feature : FeatureFlag.values()) {
			assertTrue(config.isFeatureEnabled(feature), feature.getId());
		}
	}

	@Test
	void allFeaturesEnabledWithEmptyFlags() {
		BootJavaConfig config = configWith("{\"boot-java\": {\"feature-flags\": {}}}");
		for (FeatureFlag feature : FeatureFlag.values()) {
			assertTrue(config.isFeatureEnabled(feature), feature.getId());
		}
	}

	@Test
	void featureTurnedOffOnlyByExplicitFalse() {
		BootJavaConfig config = configWith("""
				{"boot-java": {"feature-flags": {
					"value-property-completion": false,
					"bean-name-navigation": true,
					"scope-completion": "false",
					"profile-completion": 0,
					"unknown-feature": false
				}}}""");

		assertFalse(config.isFeatureEnabled(FeatureFlag.VALUE_PROPERTY_COMPLETION));
		assertTrue(config.isFeatureEnabled(FeatureFlag.BEAN_NAME_NAVIGATION));
		assertTrue(config.isFeatureEnabled(FeatureFlag.SCOPE_COMPLETION));
		assertTrue(config.isFeatureEnabled(FeatureFlag.PROFILE_COMPLETION));
		assertTrue(config.isFeatureEnabled(FeatureFlag.QUERY_TEXT_BLOCK_CONVERSION));
	}

	@Test
	void disabledFeaturesAreParsedOncePerChange() {
		BootJavaConfig config = configWith("""
				{"boot-java": {"feature-flags": {
					"value-property-completion": false,
					"scope-completion": false,
					"bean-name-navigation": true,
					"unknown-feature": false
				}}}""");

		assertEquals(Set.of(FeatureFlag.VALUE_PROPERTY_COMPLETION, FeatureFlag.SCOPE_COMPLETION), config.getDisabledFeatures());

		config.handleConfigurationChange(new Settings(null));
		assertTrue(config.getDisabledFeatures().isEmpty());
	}

	@Test
	void malformedFlagsLeaveFeaturesEnabled() {
		BootJavaConfig config = configWith("{\"boot-java\": {\"feature-flags\": [\"value-property-completion\"]}}");
		assertTrue(config.isFeatureEnabled(FeatureFlag.VALUE_PROPERTY_COMPLETION));
	}

	@Test
	void changeAppliesWithoutRestart() {
		BootJavaConfig config = configWith(null);
		assertTrue(config.isFeatureEnabled(FeatureFlag.SCOPE_COMPLETION));

		config.handleConfigurationChange(new Settings(JsonParser.parseString("{\"boot-java\": {\"feature-flags\": {\"scope-completion\": false}}}")));
		assertFalse(config.isFeatureEnabled(FeatureFlag.SCOPE_COMPLETION));
	}

	@Test
	void everyIdConstantHasAFeatureFlagAndViceVersa() throws Exception {
		Set<String> constants = new HashSet<>();
		for (Field field : FeatureFlagIds.class.getDeclaredFields()) {
			if (field.getType() == String.class) {
				constants.add((String) field.get(null));
			}
		}

		Set<String> enumIds = new HashSet<>();
		for (FeatureFlag feature : FeatureFlag.values()) {
			enumIds.add(feature.getId());
		}

		assertEquals(constants, enumIds);
	}

	@Test
	void idsAreUniqueAndResolvable() {
		Set<String> ids = new HashSet<>();
		for (FeatureFlag feature : FeatureFlag.values()) {
			assertTrue(ids.add(feature.getId()), "duplicate id: " + feature.getId());
			assertEquals(Optional.of(feature), FeatureFlag.fromId(feature.getId()));
		}
		assertEquals(Optional.empty(), FeatureFlag.fromId("no-such-feature"));
	}

}
