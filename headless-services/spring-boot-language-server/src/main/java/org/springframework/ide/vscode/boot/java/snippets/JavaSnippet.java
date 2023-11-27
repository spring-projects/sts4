/*******************************************************************************
 * Copyright (c) 2017, 2023 Pivotal, Inc.
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
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

public class JavaSnippet {

	private JavaSnippetContext context;

	private String name;
	private String template;
	private List<String> imports;

	private CompletionItemKind kind;
	private String additionalTriggerPrefix;

	public JavaSnippet(String name, JavaSnippetContext context, CompletionItemKind kind, List<String> imports,
			String template, String additionalTriggerPrefix) {
		super();
		this.context = context;
		this.name = name;
		this.template = template;
		this.imports = imports;
		this.kind = kind;
		this.additionalTriggerPrefix = additionalTriggerPrefix;
	}

	public ICompletionProposal generateCompletion(Supplier<SnippetBuilder> snippetBuilderFactory,
			DocumentRegion query, ASTNode node, CompilationUnit cu, String filterText) {

		return new JavaSnippetCompletion(snippetBuilderFactory,
				query,
				cu,
				this,
				filterText);
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

	public String getAdditionalTriggerPrefix() {
		return additionalTriggerPrefix;
	}
	
	public JavaSnippetContext getContext() {
		return context;
	}

}
