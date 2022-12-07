/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
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

import java.util.List;

import org.junit.jupiter.api.Test;

public class SpringBootUpgradeTest {

    @Test
    void recipeIdChain1() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.SpringBoot1To2Migration",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5"
        ), SpringBootUpgrade.createRecipeIdsChain(1, 3, 2, 5));
    }

    @Test
    void recipeIdChain2() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7"
        ), SpringBootUpgrade.createRecipeIdsChain(2, 2, 2, 7));
    }

    @Test
    void recipeIdChain3() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.SpringBoot1To2Migration",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7",
                "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0"
        ), SpringBootUpgrade.createRecipeIdsChain(1, 3, 3, 0));
    }

    @Test
    void recipeIdChain4() throws Exception {
        assertEquals(List.of(
                "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2"
        ), SpringBootUpgrade.createRecipeIdsChain(2, 2, 2, 2));
    }

    @Test
    void recipeIdChain5() throws Exception {
        assertEquals(List.of(
                ), SpringBootUpgrade.createRecipeIdsChain(2, 7, 2, 2));
    }
}

