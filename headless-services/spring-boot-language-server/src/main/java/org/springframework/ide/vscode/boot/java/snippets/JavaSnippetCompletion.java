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

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

public class JavaSnippetCompletion implements ICompletionProposal {

	private final DocumentRegion query;
	private final JavaSnippet javaSnippet;
	private final Supplier<SnippetBuilder> snippetBuilderFactory;
	private final CompilationUnit cu;
	private final String filterText;

	public JavaSnippetCompletion(Supplier<SnippetBuilder> snippetBuilderFactory, DocumentRegion query, CompilationUnit cu, JavaSnippet javaSnippet, String filterText) {
		this.snippetBuilderFactory = snippetBuilderFactory;
		this.query = query;
		this.cu = cu;
		this.javaSnippet = javaSnippet;
		this.filterText = filterText;
	}

	@Override
	public String getLabel() {
		return javaSnippet.getName();
	}

	@Override
	public CompletionItemKind getKind() {
		return javaSnippet.getKind();
	}

	@Override
	public DocumentEdits getTextEdit() {
		return new JavaSnippetBuilder(snippetBuilderFactory).createEdit(query, javaSnippet.getTemplate());
	}

	@Override
	public String getDetail() {
		return "Snippet";
	}

	@Override
	public Renderable getDocumentation() {
		return Renderables.NO_DESCRIPTION;
	}

	@Override
	public Optional<java.util.function.Supplier<DocumentEdits>> getAdditionalEdit() {
		return javaSnippet.getImports().map(imports -> () -> ASTUtils.getImportsEdit(cu, imports, query.getDocument()).orElse(null));
	}
	
	@Override
	public String getFilterText() {
		return this.filterText;
	}
}
