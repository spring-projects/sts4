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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.java.data.DataRepositoryDefinition;
import org.springframework.ide.vscode.boot.java.data.DomainProperty;
import org.springframework.ide.vscode.boot.java.data.DomainType;
import org.springframework.ide.vscode.boot.java.data.FindByCompletionProposal;
import org.springframework.ide.vscode.boot.java.data.providers.prefixsensitive.DataRepositoryPrefixSensitiveCompletionProvider;
import org.springframework.ide.vscode.boot.java.utils.ASTUtils;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.util.StringUtils;

import com.google.common.base.Objects;

/**
 * Provides content assist proposals for querying by a single attribute in Spring Data repositories.
 * @author Martin Lippert
 */
public class DataRepositoryStandardCompletionProvider implements DataRepositoryCompletionProvider {
	
	private static final String FIND_BY = "findBy";

	public void addProposals(Collection<ICompletionProposal> completions, IDocument doc, int offset, String prefix, DataRepositoryDefinition repo, ASTNode node) {
		if (FIND_BY.startsWith(DataRepositoryPrefixSensitiveCompletionProvider.findLastJavaIdentifierPart(prefix))) {
			DomainType domainType = repo.getDomainType();
			for (DomainProperty property : domainType.getPropertiesByName().values()) {
				completions.add(generateCompletionProposal(offset, prefix, repo, property, doc, node));
			}
		}
	}

	private ICompletionProposal generateCompletionProposal(int offset, String prefix, DataRepositoryDefinition repoDef, DomainProperty domainProperty, IDocument doc, ASTNode node) {
		Set<String> imprts = new HashSet<>();
		StringBuilder label = new StringBuilder();
		label.append("findBy");
		label.append(StringUtils.capitalize(domainProperty.getName()));
		label.append("(");
		label.append(domainProperty.getType().getSimpleName());
		label.append(" ");
		label.append(StringUtils.uncapitalize(domainProperty.getName()));
		label.append(");");

		StringBuilder completion = new StringBuilder();
		imprts.add(List.class.getName());
		completion.append("List<");
		completion.append(repoDef.getDomainType().getSimpleName());
		completion.append("> findBy");
		completion.append(StringUtils.capitalize(domainProperty.getName()));
		completion.append("(");
		completion.append(domainProperty.getType().getSimpleName());
		if (!Objects.equal(repoDef.getDomainType().getPackageName(), domainProperty.getType().getPackageName())) {
			imprts.addAll(domainProperty.getType().getUsedTypes().stream()
					.filter(repoDef.getType()::shouldImportType)
					.collect(Collectors.toList()));
		}
		completion.append(" ");
		completion.append(StringUtils.uncapitalize(domainProperty.getName()));
		completion.append(");");
		
		return FindByCompletionProposal.createProposal(offset, CompletionItemKind.Method, prefix, label.toString(), completion.toString(), false, ASTUtils.getAdditionalEdit((CompilationUnit)node.getRoot(), imprts, doc));
	}
		
}