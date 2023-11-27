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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.PrefixFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class JavaSnippetManager {

	private List<JavaSnippet> snippets = new ArrayList<>();
	private Supplier<SnippetBuilder> snippetBuilderFactory;

	private static PrefixFinder PREFIX_FINDER = new PrefixFinder() {

		@Override
		protected boolean isPrefixChar(char c) {
			return Character.isJavaIdentifierPart(c) || c == '@';
		}
	};

	public JavaSnippetManager(Supplier<SnippetBuilder> snippetBuilderFactory) {
		this.snippetBuilderFactory = snippetBuilderFactory;
	}

	public void add(JavaSnippet javaSnippet) {
		snippets.add(javaSnippet);
	}

	public void getCompletions(IDocument doc, int offset, ASTNode node, CompilationUnit cu,
			Collection<ICompletionProposal> completions) {
		// check if current offset is within the range of possible compiler problems
		if (Arrays.stream(cu.getProblems()).anyMatch(p -> p.getSourceStart() <= offset && offset <= p.getSourceEnd())) {
			return;
		}

		DocumentRegion query = PREFIX_FINDER.getPrefixRegion(doc, offset);
		boolean isEndOfDocument = offset == doc.getLength() - 1;
		
		// check if the next character is a space or a new line
		if (!isEndOfDocument) {
			try {
				char nextCharacter = doc.getChar(offset + 1);
				if (!Character.isWhitespace(nextCharacter) && nextCharacter != '\n' && nextCharacter != '\r') {
					return;
				}
			} catch (BadLocationException e) {
			}
		}

		for (JavaSnippet javaSnippet : snippets) {

			if (javaSnippet.getName().toLowerCase().startsWith(query.toString().toLowerCase())) {
				javaSnippet.generateCompletion(snippetBuilderFactory, query, node, cu, javaSnippet.getName())
						.ifPresent((completion) -> completions.add(completion));

			} else if (javaSnippet.getAdditionalTriggerPrefix().toLowerCase().startsWith(query.toString().toLowerCase())) {
				javaSnippet.generateCompletion(snippetBuilderFactory, query, node, cu, javaSnippet.getAdditionalTriggerPrefix())
						.ifPresent((completion) -> completions.add(completion));
			}
		}
	}

}
