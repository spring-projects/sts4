/*
 * Copyright 2021 the original author or authors.
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
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

public class InjectTextMavenRepositoryRecipe extends Recipe {

	private static final XPathMatcher REPOS_MATCHER = new XPathMatcher("/project/repositories");

	private String text;

	public InjectTextMavenRepositoryRecipe(String text) {
		this.text = text;
	}

	@Override
	public String getDisplayName() {
		return "Add Repository";
	}

	@Override
	public String getDescription() {
		return getDisplayName();
	}

	public TreeVisitor<?, ExecutionContext> getVisitor() {
		return new MavenIsoVisitor<ExecutionContext>() {
			public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
				Xml.Tag root = document.getRoot();
				if (!root.getChild("repositories").isPresent()) {
					document = (Xml.Document) (new AddToTagVisitor(root, Tag.build("<repositories/>")))
						.visitNonNull(document, ctx, this.getCursor().getParentOrThrow());
				}

				return super.visitDocument(document, ctx);
			}

			public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
				Xml.Tag repositories = super.visitTag(tag, ctx);
				if (REPOS_MATCHER.matches(this.getCursor())) {
					Xml.Tag repositoryTag = Tag.build(text);
					repositories = (Xml.Tag) (new AddToTagVisitor(repositories, repositoryTag))
						.visitNonNull(repositories, ctx, this.getCursor().getParentOrThrow());
					this.maybeUpdateModel();
				}
				return repositories;
			}
		};
	}

}
