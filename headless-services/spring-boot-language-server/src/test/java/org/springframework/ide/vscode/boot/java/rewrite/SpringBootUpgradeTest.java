/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ide.vscode.commons.java.Version;

public class SpringBootUpgradeTest {

	private static final Map<String, String> MINOR_VERSION_TO_RECIPE_ID = Map.of(
			"2.0", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_0",
			"2.1", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
			"2.2", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
			"2.3", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
			"2.4", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
			"2.5", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
			"2.6", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
			"2.7", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7",
			"3.0", "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0"
	);

    @Test
    void recipeIdChain_1() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_0",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5"
        ), SpringBootUpgrade.createRecipeIdsChain(1, 3, 2, 5, MINOR_VERSION_TO_RECIPE_ID));
    }

    @Test
    void recipeIdChain_2() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7"
        ), SpringBootUpgrade.createRecipeIdsChain(2, 2, 2, 7, MINOR_VERSION_TO_RECIPE_ID));
    }

    @Test
    void recipeIdChain_3() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_0",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7",
                "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0"
        ), SpringBootUpgrade.createRecipeIdsChain(1, 3, 3, 0, MINOR_VERSION_TO_RECIPE_ID));
    }

    @Test
    void recipeIdChain_4() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2"
        ), SpringBootUpgrade.createRecipeIdsChain(2, 2, 2, 2, MINOR_VERSION_TO_RECIPE_ID));
    }

    @Test
    void recipeIdChain_5() throws Exception {
        assertEquals(List.of(
                ), SpringBootUpgrade.createRecipeIdsChain(2, 7, 2, 2, MINOR_VERSION_TO_RECIPE_ID));
    }
    
    @Test
    void nearestMinorVersion() throws Exception {
    	assertEquals("3.0", SpringBootUpgrade.nearestAvailableMinorVersion(new Version(3, 0, 2, null), MINOR_VERSION_TO_RECIPE_ID.keySet()));
    	assertEquals("3.0", SpringBootUpgrade.nearestAvailableMinorVersion(new Version(3, 3, 1, null), MINOR_VERSION_TO_RECIPE_ID.keySet()));
    	assertNull(SpringBootUpgrade.nearestAvailableMinorVersion(new Version(4, 3, 1, null), MINOR_VERSION_TO_RECIPE_ID.keySet()));
    	assertNull(SpringBootUpgrade.nearestAvailableMinorVersion(new Version(1, 5, 0, null), MINOR_VERSION_TO_RECIPE_ID.keySet()));
    	assertNull(SpringBootUpgrade.nearestAvailableMinorVersion(new Version(3, 0, 2, null), Collections.emptySet()));
    }
}

