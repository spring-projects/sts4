/*
 * Copyright 2021-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.vscode.boot.java.copilot.recipe;

import java.util.regex.Pattern;

import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.AddDependency;
import org.openrewrite.maven.table.MavenMetadataFailures;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Fabian Krüger
 */
public class AddDependencyRecipeFactory extends AbstractRecipeFactory {

	/**
	 * Create {@link AddDependency} recipe from Maven dependency XML snippet.
	 *
	 * <pre>
	 * {@code
	 * <dependency>
	 *  <groupId>groupId</groupId>
	 *  <artifactId>artifactId</artifactId>
	 *  <version>${some.version}</version>
	 *  <classifier>classifier</classifier>
	 *  <scope>scope</scope>
	 *  <type>pom</type>
	 *  <optional>true</optional>
	 * </dependency>
	 * }
	 * </pre>
	 *
	 */
	public AddDependencyRecipe create(String mavenDependency) {
		try {
			JsonNode jsonNode = getJsonNode(mavenDependency);
			String groupId = getTextValue(jsonNode, "groupId");
			String artifactId = getTextValue(jsonNode, "artifactId");
			String version = getTextOrDefaultValue(jsonNode, "version", "latest");
			@Nullable
			String scope = getNullOrTextValue(jsonNode, "scope");
			@Nullable
			String type = getNullOrTextValue(jsonNode, "type");
			@Nullable
			String classifier = getNullOrTextValue(jsonNode, "classifier");
			@Nullable
			Boolean optional = Boolean.parseBoolean(getNullOrTextValue(jsonNode, "optional"));
			@Nullable
			String familyPattern = null;
			Pattern familyRegex = (familyPattern != null) ? Pattern.compile(familyPattern) : null;
			MavenMetadataFailures metadataFailures = null;

			AddDependencyRecipe recipe = new AddDependencyRecipe(groupId, artifactId, version, scope, type, classifier,
					optional, familyRegex, metadataFailures);
			return recipe;
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}

	}

}
