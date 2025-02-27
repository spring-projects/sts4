/*******************************************************************************
 * Copyright (c) 2017, 2025 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.beans;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionItemLabelDetails;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposalWithScore;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.InjectBeanCompletionRecipe;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.Renderables;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Udayani V
 * @author Alex Boyko
 */
public class BeanCompletionProposal implements ICompletionProposalWithScore {
	
	private static final String SHORT_DESCRIPTION = "inject as a bean dependency";
	
	private DocumentEdits edits;
	private IDocument doc;
	private String beanId;
	private String beanType;
	private String className;
	private RewriteRefactorings rewriteRefactorings;
	private double score;

	public BeanCompletionProposal(DocumentEdits edits, IDocument doc, String beanId, String beanType, String className,
			double score,
			RewriteRefactorings rewriteRefactorings) {
		this.edits = edits;
		this.doc = doc;
		this.beanId = beanId;
		this.beanType = beanType;
		this.className = className;
		this.score = score;
		this.rewriteRefactorings = rewriteRefactorings;
	}

	@Override
	public String getLabel() {
		return beanId;
	}

	@Override
	public CompletionItemKind getKind() {
		return CompletionItemKind.Field;
	}

	@Override
	public DocumentEdits getTextEdit() {
		return edits;
	}

	@Override
	public String getDetail() {
		return "Autowire a bean";
	}
	
	@Override
	public CompletionItemLabelDetails getLabelDetails() {
		CompletionItemLabelDetails labelDetails = new CompletionItemLabelDetails();
		labelDetails.setDescription(SHORT_DESCRIPTION);
		return labelDetails;
	}

	@Override
	public Renderable getDocumentation() {
		return Renderables.text(
				"Inject bean `%s` of type `%s` as a constructor parameter and add corresponding field".formatted(beanId, beanType));
	}

	@Override
	public Optional<Command> getCommand() {
		FixDescriptor f = new FixDescriptor(InjectBeanCompletionRecipe.class.getName(), List.of(this.doc.getUri()),"Inject bean completions")
				.withParameters(Map.of("fullyQualifiedName", beanType, "fieldName", beanId, "classFqName", className))
				.withRecipeScope(RecipeScope.NODE);
		return Optional.of(rewriteRefactorings.createFixCommand("Inject bean '%s'".formatted(beanId), f));
	}

	@Override
	public double getScore() {
		return score;
	}
	
}
