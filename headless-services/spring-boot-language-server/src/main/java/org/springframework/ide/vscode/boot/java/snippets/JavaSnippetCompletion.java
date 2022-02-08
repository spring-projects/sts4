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

import org.eclipse.lsp4j.CompletionItemKind;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.Result;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J.CompilationUnit;
import org.openrewrite.java.tree.JavaSourceFile;
import org.openrewrite.shaded.jgit.diff.Edit;
import org.openrewrite.shaded.jgit.diff.EditList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ide.vscode.boot.java.utils.JGitUtils;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.IRegion;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.base.Supplier;

public class JavaSnippetCompletion implements ICompletionProposal{
	
	private static final Logger log = LoggerFactory.getLogger(JavaSnippetCompletion.class);

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

		Recipe r = new Recipe() {
			@Override
			public String getDisplayName() {
				return "Add Imports";
			}
			
			@Override
			protected TreeVisitor<?, ExecutionContext> getVisitor() {
				return new JavaIsoVisitor<>() {
					public JavaSourceFile visitJavaSourceFile(JavaSourceFile cu, ExecutionContext p) {
						javaSnippet.getImports().ifPresent(imports -> imports.forEach(i -> maybeAddImport(i)));
						return cu;
					};
				};
			}
			
		};
	
		List<Result> results = r.run(List.of(cu));
		if (!results.isEmpty()) {
			Result result = results.get(0);
			TextDocument newDoc = new TextDocument(null, LanguageId.PLAINTEXT, 0, result.getAfter().printAll());

			EditList diff = JGitUtils.getDiff(result.getBefore().printAll(), newDoc.get());
			if (!diff.isEmpty()) {
				IDocument doc = query.getDocument();
				DocumentEdits edits = new DocumentEdits(doc, false);
				for (Edit e : diff) {
					try {
						switch(e.getType()) {
						case DELETE:
							edits.delete(doc.getLineOffset(e.getBeginA()), getStartOfLine(doc, e.getEndA()));
							break;
						case INSERT:
							edits.insert(doc.getLineOffset(e.getBeginA()), newDoc.textBetween(newDoc.getLineOfOffset(e.getBeginB()), getStartOfLine(newDoc, e.getEndB())));
							break;
						case REPLACE:
							edits.replace(doc.getLineOfOffset(e.getBeginA()), getStartOfLine(doc, e.getEndA()), newDoc.textBetween(newDoc.getLineOfOffset(e.getBeginB()), getStartOfLine(newDoc, e.getEndB())));
							break;
						case EMPTY:
							break;
						}
					} catch (BadLocationException ex) {
						log.error("Diff conversion failed", ex);
					}
				}
				return Optional.of(edits);
			}
		}
		return Optional.empty();
	}
	
	private static int getStartOfLine(IDocument doc, int lineNumber) {
		IRegion lineInformation = doc.getLineInformation(lineNumber);
		if (lineInformation != null) {
			return lineInformation.getOffset();
		}
		if (lineNumber > 0) {
			IRegion currentLine = doc.getLineInformation(lineNumber - 1);
			return currentLine.getOffset() + currentLine.getLength();
		}
		return 0;
	}
	
}
