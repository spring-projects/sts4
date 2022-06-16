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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

import org.openrewrite.config.Environment;
import org.openrewrite.config.OptionDescriptor;
import org.openrewrite.config.RecipeDescriptor;

public class RecipesDescriptionGenerator {

	public static void main(String[] args) throws IOException {
		String s = Environment.builder().scanRuntimeClasspath().build().listRecipeDescriptors().stream()
			.filter(d -> RewriteRecipeRepository.TOP_LEVEL_RECIPES.contains(d.getName()))
			.map(d -> convertToMarkdown(d, 1))
			.collect(Collectors.joining("\n\n"));
		
		Path path = Paths.get("recipes.md");
		path = path.toFile().getCanonicalFile().toPath();
		System.out.println("Saving to file: " + path);
		Files.write(path, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);

	}
	
	private static String convertToMarkdown(RecipeDescriptor d, int level) {
		StringBuilder sb = new StringBuilder();
		if (!"org.openrewrite.yaml.ChangePropertyKey".equals(d.getName())) {
			for (int i = 0; i < level; i++) {
				sb.append('#');
			}
			sb.append(' ');
			sb.append(d.getDisplayName());
			sb.append(" (");
			sb.append(d.getName());
			sb.append(")\n");
			sb.append(d.getDescription());
			sb.append("\n\n");
			for (OptionDescriptor option : d.getOptions()) {
				sb.append("- **");
				sb.append(option.getDisplayName());
				sb.append("**: ");
				sb.append(": `");
				sb.append(option.getValue());
				sb.append("`\n");
			}
			sb.append("\n");
			sb.append(d.getRecipeList().stream().map(cd -> convertToMarkdown(cd, level + 1)).collect(Collectors.joining("\n\n")));
		}
		return sb.toString();
	}

}
