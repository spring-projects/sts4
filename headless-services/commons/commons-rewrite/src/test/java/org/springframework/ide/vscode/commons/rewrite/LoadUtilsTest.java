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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openrewrite.Recipe;
import org.openrewrite.config.DeclarativeRecipe;
import org.openrewrite.config.Environment;
import org.openrewrite.config.RecipeDescriptor;
import org.openrewrite.maven.UpgradeDependencyVersion;

public class LoadUtilsTest {
	
	private static Environment env;
	
	@BeforeClass
	public static void setupAll() {
		env = Environment.builder().scanRuntimeClasspath().build();
	}
	
	@Test
	public void createRecipeTest() throws Exception {
		RecipeDescriptor recipeDescriptor = env.listRecipeDescriptors().stream().filter(d -> "org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0".equals(d.getName())).findFirst().orElse(null);
		assertNotNull(recipeDescriptor);
		Recipe r = LoadUtils.createRecipe(recipeDescriptor);
		
		assertTrue(r instanceof DeclarativeRecipe);
		assertEquals("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_0", r.getName());
		assertEquals("Upgrade to Spring Boot 3.0 from prior 2.x version.", r.getDescription());
		assertEquals("Upgrade to Spring Boot 3.0 from 2.x", r.getDisplayName());
		assertEquals(3, r.getRecipeList().size());
		
		Recipe pomRecipe = r.getRecipeList().get(0);
		assertTrue(pomRecipe instanceof DeclarativeRecipe);
		assertEquals("org.openrewrite.java.spring.boot3.MavenPomUpgrade", pomRecipe.getName());
		assertEquals("Upgrade Maven Pom to Spring Boot 3.0 from prior 2.x version.", pomRecipe.getDescription());
		assertEquals("Upgrade Maven Pom to Spring Boot 3.0 from 2.x", pomRecipe.getDisplayName());
		assertEquals(3, pomRecipe.getRecipeList().size());
		
		r = pomRecipe.getRecipeList().get(0);
		assertTrue(r instanceof UpgradeDependencyVersion);
		UpgradeDependencyVersion upgradeDependencyRecipe = (UpgradeDependencyVersion) r;
		assertEquals("org.openrewrite.maven.UpgradeDependencyVersion", upgradeDependencyRecipe.getName());
		assertEquals("Upgrade Maven dependency version", upgradeDependencyRecipe.getDisplayName());
		assertEquals(0, upgradeDependencyRecipe.getRecipeList().size());
		assertTrue(upgradeDependencyRecipe.getNewVersion().startsWith("3.0."));
		assertEquals("org.springframework.boot", upgradeDependencyRecipe.getGroupId());
		assertEquals("*", upgradeDependencyRecipe.getArtifactId());
	}
}
