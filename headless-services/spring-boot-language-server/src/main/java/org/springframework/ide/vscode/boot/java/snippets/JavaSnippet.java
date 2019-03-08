/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.snippets;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

import com.google.common.base.Supplier;

public class JavaSnippet {

	private JavaSnippetContext context;

	private String name;

	private String template;

	private List<String> imports;

	private CompletionItemKind kind;

	public JavaSnippet(String name, JavaSnippetContext context, CompletionItemKind kind, List<String> imports,
			String template) {
		super();
		this.context = context;
		this.name = name;
		this.template = template;
		this.imports = imports;
		this.kind = kind;
	}

	public Optional<ICompletionProposal> generateCompletion(Supplier<SnippetBuilder> snippetBuilderFactory,
			DocumentRegion query, ASTNode node, CompilationUnit cu) {

		if (context.appliesTo(node)) {
			return Optional.of(
					new JavaSnippetCompletion(snippetBuilderFactory,
							query,
							cu,
							this
					)
			);
		}

		return Optional.empty();
	}

	public String getName() {
		return this.name;
	}

	public String getTemplate() {
		return this.template;
	}

	public  Optional<List<String>> getImports() {
		return Optional.of(this.imports);
	}

	public CompletionItemKind getKind() {
		return kind;
	}

}
