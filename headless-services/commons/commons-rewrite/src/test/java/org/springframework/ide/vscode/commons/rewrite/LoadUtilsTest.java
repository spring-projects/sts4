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
package org.springframework.ide.vscode.commons.rewrite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openrewrite.Recipe;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.RecipeDescriptor;
import org.openrewrite.maven.UpgradeDependencyVersion;

public class LoadUtilsTest {
	
	private static Environment env;
	
	@BeforeAll
	public static void setupAll() {
		env = Environment.builder().scanRuntimeClasspath().build();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void createRecipeTest() throws Exception {
		Recipe r = env.listRecipes().stream().filter(d -> "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0".equals(d.getName())).findFirst().orElse(null);
		RecipeDescriptor recipeDescriptor = r.getDescriptor();
		assertNotNull(recipeDescriptor);
		r = LoadUtils.createRecipe(recipeDescriptor, id -> {
			try {
				return (Class<Recipe>) Class.forName(id);
			} catch (ClassNotFoundException e) {
				return null;
			}
		});
		
		assertTrue(r instanceof DeclarativeRecipe);
		assertEquals("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0", r.getName());
		assertEquals(
				"Migrate applications built on Spring Boot 2.7 to the latest Spring Boot 3.0 release. This recipe will modify an application's build files, make changes to deprecated/preferred APIs, and migrate configuration settings that have changes between versions. This recipe will also chain additional framework migrations (Spring Framework, Spring Data, etc) that are required as part of the migration to Spring Boot 2.7.\n"
						+ "",
				r.getDescription());
		assertEquals("Migrate to Spring Boot 3.0 from Spring Boot 2.7", r.getDisplayName());
		assertEquals(7, r.getRecipeList().size());
		
		Recipe pomRecipe = r.getRecipeList().get(0);
		assertTrue(pomRecipe instanceof DeclarativeRecipe);
		assertEquals("org.openrewrite.java.spring.boot3.MavenPomUpgrade", pomRecipe.getName());
		assertEquals("Upgrade Maven Pom to Spring Boot 3.0 from prior 2.x version.", pomRecipe.getDescription());
		assertEquals("Upgrade Maven Pom to Spring Boot 3.0 from 2.x", pomRecipe.getDisplayName());
		assertTrue(pomRecipe.getRecipeList().size() >= 3);
		
		UpgradeDependencyVersion upgradeDependencyRecipe = pomRecipe.getRecipeList().stream().filter(UpgradeDependencyVersion.class::isInstance).map(UpgradeDependencyVersion.class::cast).findFirst().get();
		assertEquals("org.openrewrite.maven.UpgradeDependencyVersion", upgradeDependencyRecipe.getName());
		assertEquals("Upgrade Maven dependency version", upgradeDependencyRecipe.getDisplayName());
		assertEquals(0, upgradeDependencyRecipe.getRecipeList().size());
		assertTrue(upgradeDependencyRecipe.getNewVersion().startsWith("3.0."));
		assertEquals("org.springframework.boot", upgradeDependencyRecipe.getGroupId());
		assertEquals("*", upgradeDependencyRecipe.getArtifactId());
	}
}
