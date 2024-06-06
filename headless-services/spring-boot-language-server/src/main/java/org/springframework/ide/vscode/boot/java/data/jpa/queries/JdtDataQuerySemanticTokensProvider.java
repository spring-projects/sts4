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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.springframework.ide.vscode.boot.java.JdtSemanticTokensProvider;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtDataQuerySemanticTokensProvider implements JdtSemanticTokensProvider {
	
	private static final String QUERY = "Query";
	private static final String FQN_QUERY = "org.springframework.data.jpa.repository." + QUERY;

	private final JpqlSemanticTokens jpqlProvider;
	private final HqlSemanticTokens hqlProvider;
	private final SqlSemanticTokens sqlProvider;
	private final JpqlSupportState supportState;
	
	public JdtDataQuerySemanticTokensProvider(JpqlSemanticTokens jpqlProvider, HqlSemanticTokens hqlProvider, SqlSemanticTokens sqlSemanticTokens, JpqlSupportState supportState) {
		this.jpqlProvider = jpqlProvider;
		this.hqlProvider = hqlProvider;
		this.sqlProvider = sqlSemanticTokens;
		this.supportState = supportState;
	}

	@Override
	public List<String> getTokenTypes() {
		return Stream.concat(jpqlProvider.getTokenTypes().stream(), hqlProvider.getTokenTypes().stream()).distinct().collect(Collectors.toList());
	}

	@Override
	public List<String> getTokenModifiers() {
		return Stream.concat(jpqlProvider.getTypeModifiers().stream(), hqlProvider.getTypeModifiers().stream()).distinct().collect(Collectors.toList());
	}

	@Override
	public ASTVisitor getTokensComputer(IJavaProject jp, TextDocument doc, CompilationUnit cu, Collector<SemanticTokenData> tokensData) {
		SemanticTokensDataProvider provider = SpringProjectUtil.hasDependencyStartingWith(jp, "hibernate-core", null) ? hqlProvider : jpqlProvider;
		return new ASTVisitor() {
			@Override
			public boolean visit(NormalAnnotation a) {
				if (isQueryAnnotation(a)) {
					List<?> values = a.values();
					
					Expression queryExpression = null;
					boolean isNative = false;
					for (Object value : values) {
						if (value instanceof MemberValuePair) {
							MemberValuePair pair = (MemberValuePair) value;
							String name = pair.getName().getFullyQualifiedName();
							if (name != null) {
								switch (name) {
								case "value":
									queryExpression = pair.getValue();
									break;
								case "nativeQuery":
									Expression expression = pair.getValue();
									if (expression != null) {
										Object o = expression.resolveConstantExpressionValue();
										if (o instanceof Boolean b) {
											isNative = b.booleanValue();
										}
									}
									break;
								}
							}
						}
					}
					
					if (queryExpression != null) {
						if (isNative) {
							computeTokensForQueryExpression(sqlProvider, queryExpression).forEach(tokensData::accept);
						} else {
							computeTokensForQueryExpression(provider, queryExpression).forEach(tokensData::accept);
						}
					}
					
					return false;
				}
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation a) {
				if (isQueryAnnotation(a)) {
					computeTokensForQueryExpression(provider, a.getValue()).forEach(tokensData::accept);
				}
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				if ("createQuery".equals(node.getName().getIdentifier()) && node.arguments().size() <= 2 && node.arguments().get(0) instanceof Expression queryExpr) {
					IMethodBinding methodBinding = node.resolveMethodBinding();
					if ("jakarta.persistence.EntityManager".equals(methodBinding.getDeclaringClass().getQualifiedName())) {
						if (methodBinding.getParameterTypes().length <= 2 && "java.lang.String".equals(methodBinding.getParameterTypes()[0].getQualifiedName())) {
							computeTokensForQueryExpression(provider, queryExpr).forEach(tokensData::accept);
						}
					}
				}
				return super.visit(node);
			}
		};
	}
	
	private static List<SemanticTokenData> computeTokensForQueryExpression(SemanticTokensDataProvider provider, Expression valueExp) {
		String query = null;
		int offset = 0;
		if (valueExp instanceof StringLiteral sl) {
			query = sl.getEscapedValue();
			query = query.substring(1, query.length() - 1);
			offset = sl.getStartPosition() + 1; // +1 to skip over opening "
		} else if (valueExp instanceof TextBlock tb) {
			query = tb.getEscapedValue();
			query = query.substring(3, query.length() - 3);
			offset = tb.getStartPosition() + 3; // +3 to skip over opening """ 
		}
		
		if (query != null) {
			return provider.computeTokens(query, offset);
		}
		return Collections.emptyList();
	}

	
	private static boolean isQueryAnnotation(Annotation a) {
		return FQN_QUERY.equals(a.getTypeName().getFullyQualifiedName())
				|| QUERY.equals(a.getTypeName().getFullyQualifiedName());
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return supportState.isEnabled() && SpringProjectUtil.hasDependencyStartingWith(project, "spring-data-jpa", null);
	}

}
