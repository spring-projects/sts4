/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.xml;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.IType;
import org.springframework.ide.vscode.commons.languageserver.completion.DocumentEdits;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.util.Renderable;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

/**
 * @author Martin Lippert
 */
public class TypeCompletionProposalProvider implements XMLCompletionProvider {

	private final JavaProjectFinder projectFinder;

	public TypeCompletionProposalProvider(JavaProjectFinder projectFinder) {
		this.projectFinder = projectFinder;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument doc, String namespace, DOMNode node, DOMAttr attributeAt,
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

			Flux<Tuple2<IType, Double>> types = project.getIndex().fuzzySearchTypes(prefix, true, true);

			return types.collectSortedList((o1, o2) -> o2.getT2().compareTo(o1.getT2()))
				.flatMapIterable(l -> l)
				.filter(result -> result.getT1() != null && result.getT1().getElementName() != null && result.getT1().getElementName().length() > 0)
				.map(t -> createProposal(t, doc, offset, tokenOffset, tokenEnd)).collectList().block();
		};

		return null;
	}

	private ICompletionProposal createProposal(Tuple2<IType, Double> t, TextDocument doc, int offset, int tokenStart, int tokenEnd) {
		IType type = t.getT1();

		String label = type.getElementName();
		CompletionItemKind kind;

		if (type.isClass()) {
			kind = CompletionItemKind.Class;
		}
		else if (type.isInterface()) {
			kind = CompletionItemKind.Interface;
		}
		else {
			kind = CompletionItemKind.Value;
		}

		DocumentEdits edits = new DocumentEdits(doc);
		edits.replace(tokenStart, tokenEnd, "\"" + type.getFullyQualifiedName() + "\"");

		Renderable renderable = null;

		return new TypeCompletionProposal(label, kind, edits, type.getFullyQualifiedName(), renderable);
	}

}
