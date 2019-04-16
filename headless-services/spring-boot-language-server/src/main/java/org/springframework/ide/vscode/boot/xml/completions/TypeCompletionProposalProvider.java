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
package org.springframework.ide.vscode.boot.xml.completions;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4xml.dom.DOMAttr;
import org.eclipse.lsp4xml.dom.DOMNode;
import org.eclipse.lsp4xml.dom.parser.Scanner;
import org.springframework.ide.vscode.boot.xml.XMLCompletionProvider;
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
	private final boolean classesOnly;

	public TypeCompletionProposalProvider(JavaProjectFinder projectFinder, boolean classesOnly) {
		this.projectFinder = projectFinder;
		this.classesOnly = classesOnly;
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

//			Flux<Tuple2<IType, Double>> types = project.getIndex().fuzzySearchTypes(prefix, true, true);
			Flux<Tuple2<IType, Double>> types = project.getIndex().camelcaseSearchTypes(prefix, true, true);

			final String prefixStr = prefix;

			return types
				.filter(result -> result.getT1() != null && result.getT1().getElementName() != null && result.getT1().getElementName().length() > 0)
				.filter(result -> classesOnly ? result.getT1().isClass() : true)
				.map(t -> createProposal(t, doc, offset, prefixStr))
				.collectList().block();
		};

		return Collections.emptyList();
	}

	private ICompletionProposal createProposal(Tuple2<IType, Double> t, TextDocument doc, int offset, String prefix) {
		IType type = t.getT1();

		String label = type.getFullyQualifiedName();
		int splitIndex = Math.max(label.lastIndexOf("."), label.lastIndexOf("$"));

		if (splitIndex > 0) {
			label = label.substring(splitIndex + 1) + " - " + label.substring(0, splitIndex);
		}

		CompletionItemKind kind;
		if (type.isClass()) {
			kind = CompletionItemKind.Class;
		}
		else if (type.isInterface()) {
			kind = CompletionItemKind.Interface;
		}
		else if (type.isEnum()) {
			kind = CompletionItemKind.Enum;
		}
		else {
			kind = CompletionItemKind.Property;
		}

		DocumentEdits edits = new DocumentEdits(doc);
		edits.delete(offset - prefix.length(), offset);
		edits.insert(offset, type.getFullyQualifiedName());

		Renderable renderable = null;

		return new TypeCompletionProposal(label, kind, edits, type.getFullyQualifiedName(), renderable, t.getT2());
	}

}
