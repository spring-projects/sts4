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
package org.springframework.ide.vscode.boot.java.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.J.Annotation;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.ide.vscode.boot.java.snippets.JavaSnippetManager;
import org.springframework.ide.vscode.boot.java.utils.ORAstUtils;
import org.springframework.ide.vscode.boot.java.utils.ORCompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionEngine;
import org.springframework.ide.vscode.commons.languageserver.completion.ICompletionProposal;
import org.springframework.ide.vscode.commons.languageserver.util.LanguageSpecific;
import org.springframework.ide.vscode.commons.util.text.IDocument;
import org.springframework.ide.vscode.commons.util.text.LanguageId;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

import com.google.common.collect.ImmutableList;

/**
 * @author Martin Lippert
 */
public class BootJavaCompletionEngine implements ICompletionEngine, LanguageSpecific {

	private Map<String, CompletionProvider> completionProviders;
	private JavaSnippetManager snippets;
	private ORCompilationUnitCache cuCache;

	public BootJavaCompletionEngine(ORCompilationUnitCache cuCache, Map<String, CompletionProvider> specificProviders, JavaSnippetManager snippets) {
		this.cuCache = cuCache;
		this.completionProviders = specificProviders;
		this.snippets = snippets;
	}

	@Override
	public Collection<ICompletionProposal> getCompletions(TextDocument document, int offset) throws Exception {
		return cuCache.withCompilationUnit(document, cu -> {
			if (cu != null) {
				J node = ORAstUtils.findAstNodeAt(cu, offset);

				if (node != null) {
					Collection<ICompletionProposal> completions = new ArrayList<ICompletionProposal>();
					collectCompletionsForAnnotations(node, offset, document, completions);
					collectCompletions(node, offset, document, completions);
					snippets.getCompletions(document, offset, node, cu, completions);
					return completions;
				}
			}

			return Collections.emptyList();
		});
	}

	private void collectCompletionsForAnnotations(J node, int offset, IDocument doc, Collection<ICompletionProposal> completions) {
		Annotation annotation = ORAstUtils.findNode(node, Annotation.class);
		J exactNode = node;

		if (annotation != null) {
			FullyQualified type = TypeUtils.asFullyQualified(annotation.getType());
			if (type != null) {
				String qualifiedName = type.getFullyQualifiedName();
				if (qualifiedName != null) {
					CompletionProvider provider = this.completionProviders.get(qualifiedName);
					if (provider != null) {
						provider.provideCompletions(exactNode, annotation, offset, doc, completions);
					}
				}
			}
		}
	}

	private void collectCompletions(J node, int offset, TextDocument document, Collection<ICompletionProposal> completions) {
		if (node != null) {
			for (CompletionProvider completionProvider : this.completionProviders.values()) {
				completionProvider.provideCompletions(node, offset, document, completions);
			}
		}
	}

	@Override
	public Collection<LanguageId> supportedLanguages() {
		return ImmutableList.of(LanguageId.JAVA);
	}

}
