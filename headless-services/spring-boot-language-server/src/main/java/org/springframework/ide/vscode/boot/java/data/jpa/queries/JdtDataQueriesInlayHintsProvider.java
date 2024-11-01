/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.data.jpa.queries;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.lsp4j.InlayHint;
import org.eclipse.lsp4j.InlayHintKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.springframework.ide.vscode.boot.java.JdtInlayHintsProvider;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JdtQueryVisitorUtils.EmbeddedQueryExpression;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.util.BadLocationException;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtDataQueriesInlayHintsProvider implements JdtInlayHintsProvider {
	
	private final JdtDataQuerySemanticTokensProvider semanticTokensProvider;

	
	public JdtDataQueriesInlayHintsProvider(JdtDataQuerySemanticTokensProvider semanticTokensProvider) {
		this.semanticTokensProvider = semanticTokensProvider;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return semanticTokensProvider.isApplicable(project);
	}

	@Override
	public ASTVisitor getInlayHintsComputer(IJavaProject project, TextDocument doc, CompilationUnit cu,
			Collector<InlayHint> collector) {
		return new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation node) {
				if (node.getParent() instanceof MethodDeclaration m && !m.parameters().isEmpty()) {
					EmbeddedQueryExpression q = JdtQueryVisitorUtils.extractQueryExpression(node);
					if (q != null) {
						processQuery(project, doc, collector, m, q);
					}
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				if (node.getParent() instanceof MethodDeclaration m && !m.parameters().isEmpty()) {
					EmbeddedQueryExpression q = JdtQueryVisitorUtils.extractQueryExpression(node);
					if (q != null) {
						processQuery(project, doc, collector, m, q);
					}
				}
				return super.visit(node);
			}
			
		};
	}
	
	private void processQuery(IJavaProject project, TextDocument doc, Collector<InlayHint> collector, MethodDeclaration m, EmbeddedQueryExpression q) {
		List<SemanticTokenData> semanticTokens = semanticTokensProvider.computeSemanticTokens(project, q.query(), q.isNative());
		SemanticTokenData previousToken = null;
		for (SemanticTokenData t : semanticTokens) {
			if (isValidParameterOrdinalInputParameterToken(doc, t, previousToken)) {
				try {
					int number = Integer.parseInt(doc.get(t.range().getOffset(), t.range().getLength()));
					if (number > 0 && number <= m.parameters().size()) {
						Object param = m.parameters().get(number - 1);
						if (param instanceof SingleVariableDeclaration svd) {
							String paramName = svd.getName().getIdentifier();
							InlayHint hint = new InlayHint();
							hint.setKind(InlayHintKind.Parameter);
							hint.setLabel(Either.forLeft(paramName));
							hint.setPaddingLeft(true);
							hint.setPaddingRight(true);
							hint.setPosition(doc.toPosition(firstNonSkippedChar(doc, t.range().getEnd(), c -> '%' != c)));
							
							collector.accept(hint);
						}
					}
				} catch (NumberFormatException e) {
					// ignore
				} catch (BadLocationException e) {
					// ignore
				}
			}
			previousToken = t;
		}
	}
	
	/*
	 * Determines if token is parameter token and previous token is a '?' operator - valid syntax for method parameter ordinal
	 */
	private static boolean isValidParameterOrdinalInputParameterToken(TextDocument doc, SemanticTokenData token, SemanticTokenData previous) {
		if ("parameter".equals(token.type()) && previous != null && "operator".equals(previous.type())) {
			try {
				return "?".equals(doc.get(previous.range().getOffset(), previous.range().getLength()));
			} catch (BadLocationException e) {
				// ignore
			}
		}
		return false;
	}
	
	private static int firstNonSkippedChar(TextDocument doc, int offset, Predicate<Character> skip) {
		try {
			int i = offset;
			for (; i < doc.getLength() && !skip.test(doc.getChar(i)); i++) {}
			return i;
		} catch (BadLocationException e) {
			// ignore - shouldn't happen
		}
		return doc.getLength();
	}

}
