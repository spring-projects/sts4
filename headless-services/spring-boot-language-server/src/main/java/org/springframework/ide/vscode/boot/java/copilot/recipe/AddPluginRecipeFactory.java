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
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.AddPlugin;

/**
 * @author Fabian Krüger
 */
public class AddPluginRecipeFactory extends AbstractRecipeFactory {

	/**
	 * Create {@link AddPlugin} from Maven plugin XML snippet.
	 */
	public AddPlugin create(String buildPlugin) {
		try {
			JsonNode jsonNode = getJsonNode(buildPlugin);
			String groupId = getTextValue(jsonNode, "groupId");
			String artifactId = getTextValue(jsonNode, "artifactId");
			@Nullable
			String version = getNullOrTextValue(jsonNode, "version");
			@Nullable
			String configuration = getNullOrTextValue(jsonNode, "configuration");
			@Nullable
			String dependencies = getNullOrTextValue(jsonNode, "dependencies");
			@Nullable
			String executions = getNullOrTextValue(jsonNode, "executions");
			@Nullable
			String filePattern = getNullOrTextValue(jsonNode, "filePattern");
			AddPlugin addPlugin = new AddPlugin(groupId, artifactId, version, configuration, dependencies, executions,
					filePattern);
			return addPlugin;
		}
		catch (JsonProcessingException ex) {
			throw new RuntimeException(ex);
		}
	}

}
