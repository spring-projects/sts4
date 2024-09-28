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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.AddManagedDependencyVisitor;

/**
 * Alternative to {@link org.openrewrite.maven.AddManagedDependency} that doesn't verify
 * the provided version. It uses the {@link AddManagedDependencyVisitor} and bypasses all
 * checks and other code in {@link org.openrewrite.maven.AddManagedDependency}.
 *
 * @author Fabian Krüger
 */
public class AddManagedDependencyRecipe extends Recipe {

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String scope;

	private final String type;

	private final String classifier;

	public AddManagedDependencyRecipe(String groupId, String artifactId, String version, String scope, String type,
			String classifier) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.version = version;
		this.scope = scope;
		this.type = type;
		this.classifier = classifier;
	}

	@Override
	public String getDisplayName() {
		return "Add managed dependency '%s:%s'".formatted(groupId, artifactId);
	}

	@Override
	public String getDescription() {
		return getDisplayName();
	}

	@Override
	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new AddManagedDependencyVisitor(groupId, artifactId, version, scope, type, classifier);
	}

}
