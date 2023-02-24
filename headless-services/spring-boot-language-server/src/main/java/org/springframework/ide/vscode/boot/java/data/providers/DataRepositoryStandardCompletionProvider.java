/*******************************************************************************
 * Copyright (c) 2023 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.providers;

import java.util.Collection;

import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryDefinition;
import org.springframework.ide.vscode.boot.java.data.DomainProperty;
import org.springframework.ide.vscode.boot.java.data.DomainType;
import org.springframework.ide.vscode.boot.java.data.FindByCompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.util.StringUtils;

/**
 * Provides content assist proposals for querying by a single attribute in Spring Data repositories.
 * @author Martin Lippert
 */
public class DataRepositoryStandardCompletionProvider implements DataRepositoryCompletionProvider {

	public void addProposals(Collection<ICompletionProposal> completions, IDocument doc, int offset, String prefix, DataRepositoryDefinition repo) {
		DomainType domainType = repo.getDomainType();
		DomainProperty[] properties = domainType.getProperties();
		for (DomainProperty property : properties) {
			completions.add(generateCompletionProposal(offset, prefix, repo, property));
		}
	}

	private ICompletionProposal generateCompletionProposal(int offset, String prefix, DataRepositoryDefinition repoDef, DomainProperty domainProperty) {
		StringBuilder label = new StringBuilder();
		label.append("findBy");
		label.append(StringUtils.capitalize(domainProperty.getName()));
		label.append("(");
		label.append(domainProperty.getType().getSimpleName());
		label.append(" ");
		label.append(StringUtils.uncapitalize(domainProperty.getName()));
		label.append(");");

		StringBuilder completion = new StringBuilder();
		completion.append("List<");
		completion.append(repoDef.getDomainType().getSimpleName());
		completion.append("> findBy");
		completion.append(StringUtils.capitalize(domainProperty.getName()));
		completion.append("(");
		completion.append(domainProperty.getType().getSimpleName());
		completion.append(" ");
		completion.append(StringUtils.uncapitalize(domainProperty.getName()));
		completion.append(");");

		return FindByCompletionProposal.createProposal(offset, CompletionItemKind.Method, prefix, label.toString(), completion.toString());
	}
}