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
package org.springframework.ide.vscode.boot.java.cron;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.JdtSemanticTokensProvider;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JdtDataQuerySemanticTokensProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtCronSemanticTokensProvider implements JdtSemanticTokensProvider {
	
	final private CronSemanticTokens tokensProvider;
	
	static final String SCHEDULED_SIMPLE_NAME = "Scheduled";

	
	public JdtCronSemanticTokensProvider(CronSemanticTokens tokensProvider) {
		this.tokensProvider = tokensProvider;
	}

	@Override
	public List<String> getTokenTypes() {
		return tokensProvider.getTokenTypes();
	}

	@Override
	public List<String> getTokenModifiers() {
		return tokensProvider.getTypeModifiers();
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "spring-context", null);
	}

	@Override
	public ASTVisitor getTokensComputer(IJavaProject project, TextDocument doc, CompilationUnit cu,
			Collector<SemanticTokenData> collector) {
		return new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation node) {
				if (node.getTypeName() != null) {
					String fqn = node.getTypeName().getFullyQualifiedName();
					if (SCHEDULED_SIMPLE_NAME.equals(fqn) || Annotations.SCHEDULED.equals(fqn)) {
						ITypeBinding typeBinding = node.resolveTypeBinding();
						if (typeBinding != null && Annotations.SCHEDULED.equals(typeBinding.getQualifiedName())) {
							for (Object value : node.values()) {
								if (value instanceof MemberValuePair) {
									MemberValuePair pair = (MemberValuePair) value;
									String name = pair.getName().getFullyQualifiedName();
									if (name != null && "cron".equals(name) && isCronExpression(pair.getValue())) {
										JdtDataQuerySemanticTokensProvider.computeTokensForExpression(tokensProvider, pair.getValue()).forEach(collector::accept);
									}
								}
							}
						}
					}
				}
				return super.visit(node);
			}
			
		};
	}
	
	public static boolean isCronExpression(Expression e) {
		String value = null;
		if (e instanceof StringLiteral sl) {
			value = sl.getLiteralValue();
		} else if (e instanceof TextBlock tb) {
			value = tb.getLiteralValue();
		}
		value = value.trim();
		if (value.startsWith("#{") || value.startsWith("${")) {
			// Either SPEL or Property Holder
			return false;
		}
		return value != null;
	}

}
