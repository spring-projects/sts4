/*******************************************************************************
 * Copyright (c) 2017, 2018 Pivotal, Inc.
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
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.PrefixFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SnippetBuilder;
import org.springframework.ide.vscode.commons.util.FuzzyMatcher;
import org.springframework.ide.vscode.commons.util.text.DocumentRegion;
import org.springframework.ide.vscode.commons.util.text.IDocument;

import com.google.common.base.Supplier;

public class JavaSnippetManager {

	private List<JavaSnippet> snippets = new ArrayList<>();
	private Supplier<SnippetBuilder> snippetBuilderFactory;

	private static PrefixFinder PREFIX_FINDER = new PrefixFinder() {

		@Override
		protected boolean isPrefixChar(char c) {
			return Character.isJavaIdentifierPart(c);
		}
	};

	public JavaSnippetManager(Supplier<SnippetBuilder> snippetBuilderFactory) {
		this.snippetBuilderFactory = snippetBuilderFactory;
	}

	public void add(JavaSnippet javaSnippet) {
		snippets.add(javaSnippet);

	}

	public void getCompletions(IDocument doc, int offset, ASTNode node, CompilationUnit cu, Collection<ICompletionProposal> completions) {
		DocumentRegion query = PREFIX_FINDER.getPrefixRegion(doc, offset);

		for (JavaSnippet javaSnippet : snippets) {
			if (FuzzyMatcher.matchScore(query.toString(), javaSnippet.getName()) != 0) {
				javaSnippet.generateCompletion(snippetBuilderFactory, query, node, cu)
						.ifPresent((completion) -> completions.add(completion));
			}
		}
	}

}
