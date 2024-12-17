/*******************************************************************************
 * Copyright (c) 2019, 2024 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml.completions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.lemminx.dom.DOMAttr;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lemminx.dom.parser.Scanner;
import org.eclipse.lsp4j.CompletionItemKind;
import org.springframework.ide.vscode.boot.index.SpringMetamodelIndex;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.completion.InternalCompletionList;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.protocol.spring.Bean;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.util.function.Tuples;

/**
 * @author Martin Lippert
 */
public class BeanRefCompletionProposalProvider implements XMLCompletionProvider {

	private final JavaProjectFinder projectFinder;
	private final SpringMetamodelIndex index;

	public BeanRefCompletionProposalProvider(JavaProjectFinder projectFinder, SpringMetamodelIndex index) {
		this.projectFinder = projectFinder;
		this.index = index;
	}

	@Override
	public InternalCompletionList getCompletions(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt,
			Scanner scanner, int offset) {

		int tokenOffset = scanner.getTokenOffset();
		int tokenEnd = scanner.getTokenEnd();
		String tokenText = scanner.getTokenText();

		Optional<IJavaProject> foundProject = this.projectFinder.find(doc.getId());
		if (foundProject.isPresent()) {
			IJavaProject project = foundProject.get();

			String prefix = tokenText.substring(0, offset - tokenOffset);
			if (prefix.startsWith("\"")) {
				prefix = prefix.substring(1);
			}

			final String searchPrefix = prefix;

			Bean[] beansOfProject = this.index.getBeansOfProject(project.getElementName());
			List<ICompletionProposal> completionItems = Arrays.stream(beansOfProject)
				.map(bean -> bean.getName())
				.filter(beanID -> beanID != null && beanID.length() > 0)
				.map(beanID -> Tuples.of(beanID, FuzzyMatcher.matchScore(searchPrefix, beanID)))
				.filter(tuple -> tuple.getT2() != 0.0)
				.map(tuple -> createProposal(tuple.getT1(), doc, offset, searchPrefix, tuple.getT2()))
				.collect(Collectors.toList());
			
			return new InternalCompletionList(completionItems, false);
		};

		return new InternalCompletionList(Collections.emptyList(), false);
	}

	private ICompletionProposal createProposal(String beanID, TextDocument doc, int offset, String prefix, Double score) {
		CompletionItemKind kind = CompletionItemKind.Reference;

		DocumentEdits edits = new DocumentEdits(doc, false);
		edits.replace(offset - prefix.length(), offset, beanID);

		Renderable renderable = null;

		return new GenericXMLCompletionProposal(beanID, kind, edits, beanID, renderable, score);
	}

}
