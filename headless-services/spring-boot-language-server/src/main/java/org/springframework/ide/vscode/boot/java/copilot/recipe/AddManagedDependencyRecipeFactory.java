/*
 * Copyright 2021-2023 the original author or authors.
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.maven.model.Dependency;
import org.openrewrite.internal.lang.Nullable;

/**
 * @author Fabian Krüger
 */
public class AddManagedDependencyRecipeFactory extends AbstractRecipeFactory {

	/**
	 * Create a {@link AddManagedDependencyRecipe} from a Maven dependency XML snippet.
	 *
	 * <pre>
	 * {@code
	 * <dependency>
	 *  <groupId>groupId</groupId>
	 *  <artifactId>artifactId</artifactId>
	 *  <version>${some.version}</version>
	 *  <classifier>classifier</classifier>
	 *  <type>pom</type>
	 * </dependency>
	 * }
	 * </pre>
	 */
	public AddManagedDependencyRecipe create(String mavenDependencySnippet) {
		try {
			JsonNode jsonNode = getJsonNode(mavenDependencySnippet);
			String groupId = getTextValue(jsonNode, "groupId");
			String artifactId = getTextValue(jsonNode, "artifactId");
			@Nullable
			String version = getNullOrTextValue(jsonNode, "version");
			@Nullable
			String scope = getNullOrTextValue(jsonNode, "scope");
			@Nullable
			String classifier = getNullOrTextValue(jsonNode, "classifier");
			@Nullable
			String type = getNullOrTextValue(jsonNode, "type");
			AddManagedDependencyRecipe addManagedDependency = new AddManagedDependencyRecipe(groupId, artifactId,
					version, scope, type, classifier);
			return addManagedDependency;
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Create a {@link AddManagedDependencyRecipe} from a {@link Dependency}.
	 */
	public AddManagedDependencyRecipe create(Dependency dependency) {
		String groupId = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion();
		String scope = dependency.getScope();
		String type = dependency.getType();
		String classifier = dependency.getClassifier();
		AddManagedDependencyRecipe addManagedDependency = new AddManagedDependencyRecipe(groupId, artifactId, version,
				scope, type, classifier);
		return addManagedDependency;
	}

}
