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

import java.util.Optional;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.jdt.imports.ImportRewrite;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;

import com.google.common.base.Supplier;

public class JavaSnippetCompletion implements ICompletionProposal{

	private DocumentRegion query;
	private JavaSnippet javaSnippet;
	private Supplier<SnippetBuilder> snippetBuilderFactory;
	private CompilationUnit cu;

	public JavaSnippetCompletion(Supplier<SnippetBuilder> snippetBuilderFactory, DocumentRegion query, CompilationUnit cu, JavaSnippet javaSnippet) {
		this.snippetBuilderFactory = snippetBuilderFactory;
		this.query = query;
		this.cu = cu;
		this.javaSnippet = javaSnippet;
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
	public Optional<DocumentEdits> getAdditionalEdit() {
		ImportRewrite rewrite =  ImportRewrite.create(cu, true);

		javaSnippet.getImports().ifPresent((imprts ->
		{
			for (String imprt : imprts) {
				rewrite.addImport(imprt);
			}
		}));

		DocumentEdits edit = rewrite.createEdit(query.getDocument());

		return edit != null ?  Optional.of(edit) : Optional.empty();
	}
}
