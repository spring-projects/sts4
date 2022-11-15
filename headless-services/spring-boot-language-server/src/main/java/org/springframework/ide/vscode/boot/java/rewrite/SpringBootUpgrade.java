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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.openrewrite.Recipe;
import org.openrewrite.config.DeclarativeRecipe;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.Assert;

import com.google.gson.JsonElement;

public class SpringBootUpgrade {
	
	final public static String CMD_UPGRADE_SPRING_BOOT = "sts/upgrade/spring-boot";

	private static final Map<String, String> VERSION_TO_RECIPE_ID = Map.of(
			"2.0", "org.openrewrite.java.spring.boot2.SpringBoot1To2Migration",
			"2.1", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_1",
			"2.2", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_2",
			"2.3", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_3",
			"2.4", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_4",
			"2.5", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_5",
			"2.6", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_6",
			"2.7", "org.openrewrite.java.spring.boot2.UpgradeSpringBoot_2_7",
			"3.0", "org.springframework.sts.java.spring.boot3.UpgradeSpringBoot_3_0"
	);
	
	private RewriteRecipeRepository recipeRepo;

	public SpringBootUpgrade(SimpleLanguageServer server, RewriteRecipeRepository recipeRepo, JavaProjectFinder projectFinder) {
		this.recipeRepo = recipeRepo;
		
		server.onCommand(CMD_UPGRADE_SPRING_BOOT, params -> {
			String uri = ((JsonElement) params.getArguments().get(0)).getAsString();
			Assert.isLegal(uri != null, "Project URI parameter must not be 'null'");
			Version targetVersion = SpringProjectUtil.getVersion(((JsonElement) params.getArguments().get(1)).getAsString());
			Assert.isLegal(targetVersion != null, "Target Spring Boot version must not be 'null'");
			
			IJavaProject project = projectFinder.find(new TextDocumentIdentifier(uri)).orElse(null);
			Assert.isLegal(project != null, "No Spring Boot project found for uri: " + uri);
			
			Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
			
			// Version upgrade is not supposed to work for patch version. Only for the major and minor versions.
			
			Assert.isLegal(
					version.getMajor() < targetVersion.getMajor() || (version.getMajor() == targetVersion.getMajor()
							&& version.getMinor() < targetVersion.getMinor()),
					"Cannot upgrade Spring Boot Project '" + project.getElementName() + "' because its version '"
							+ version.toMajorMinorVersionStr() + "' is newer than target version '"
							+ targetVersion.toMajorMinorVersionStr() + "'");
			
			return recipeRepo.loaded.thenComposeAsync(loade -> recipeRepo.apply(
					createUpgradeRecipe(version.getMajor(), version.getMinor(), targetVersion.getMajor(), targetVersion.getMinor()),
					uri,
					UUID.randomUUID().toString()
			));
		});
	}
	
	static List<String> createRecipeIdsChain(int major, int minor, int targetMajor, int targetMinor) {
		List<String> ids = new ArrayList<>();
		for (int currentMajor = major, currentMinor = minor + 1; targetMajor >  currentMajor || (targetMajor == currentMajor && currentMinor <= targetMinor);) {
			String recipeId = VERSION_TO_RECIPE_ID.get(createVersionString(currentMajor, currentMinor));
			if (recipeId == null) {
				currentMajor++;
				currentMinor = 0;
			} else {
				ids.add(recipeId);
				currentMinor++;
			}
		}
		return ids;
	}
	
	private Recipe createUpgradeRecipe(int major, int minor, int targetMajor, int targetMinor) {
		Recipe recipe = new DeclarativeRecipe("upgrade-spring-boot", "Upgrade Spring Boot from " + createVersionString(major, minor) + " to " + createVersionString(targetMajor, targetMinor),
				"", Collections.emptySet(), null, null, false);
		
		createRecipeIdsChain(major, minor, targetMajor, targetMinor).stream()
				.map(recipeRepo::getRecipe)
				.filter(o -> o.isPresent())
				.forEach(o -> recipe.doNext(o.get()));

		if (recipe.getRecipeList().isEmpty()) {
			throw new IllegalStateException("No upgrade recipes found!");
		} else if (recipe.getRecipeList().size() == 1) {
			return recipe.getRecipeList().get(0);
		} else {
			return recipe;
		}
	}
	
	private static String createVersionString(int major, int minor) {
		StringBuilder sb = new StringBuilder();
		sb.append(major);
		sb.append('.');
		sb.append(minor);
		return sb.toString();
	}
	

}
