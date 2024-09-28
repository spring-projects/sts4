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
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author Fabian Krüger
 */
public class AbstractRecipeFactory {

	private static XmlMapper mapper = new XmlMapper();

	@org.jetbrains.annotations.Nullable
	protected static String getNullOrTextValue(JsonNode jsonNode, String field) {
		return (jsonNode.get(field) != null) ? AbstractRecipeFactory.getTextValue(jsonNode, field) : null;
	}

	protected static String getTextValue(JsonNode jsonNode, String field) {
		try {
			JsonNode xmlNode = jsonNode.get(field);
			return xmlNode.textValue();
		}
		catch (NullPointerException npe) {
			throw new RecipeCreationException(
					"Could not get text value for field '%s' from: \n%s".formatted(field, jsonNode.toPrettyString()));
		}
	}

	protected static JsonNode getJsonNode(String mavenDependencySnippet) throws JsonProcessingException {
		JsonNode jsonNode = mapper.readTree(mavenDependencySnippet);
		return jsonNode;
	}

	protected String getTextOrDefaultValue(JsonNode jsonNode, String version, String defaultValue) {
		String nullOrTextValue = getNullOrTextValue(jsonNode, version);
		return (nullOrTextValue != null) ? nullOrTextValue : defaultValue;
	}

}
