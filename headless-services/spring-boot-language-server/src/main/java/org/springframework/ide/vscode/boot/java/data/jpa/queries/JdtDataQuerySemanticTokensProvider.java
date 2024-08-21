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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.JdtSemanticTokensProvider;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.spel.SpelSemanticTokens;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokenData;
import org.springframework.ide.vscode.commons.languageserver.semantic.tokens.SemanticTokensDataProvider;
import org.springframework.ide.vscode.commons.util.Collector;
import org.springframework.ide.vscode.commons.util.text.TextDocument;

public class JdtDataQuerySemanticTokensProvider implements JdtSemanticTokensProvider {
	
	private static final String QUERY = "Query";
	private static final String NAMED_QUERY = "NamedQuery";	

	private final JpqlSemanticTokens jpqlProvider;
	private final HqlSemanticTokens hqlProvider;
	private final JpqlSupportState supportState;
	private final Map<SqlType, SemanticTokensDataProvider> sqlTokenProviders;

	
	public JdtDataQuerySemanticTokensProvider(JpqlSemanticTokens jpqlProvider, HqlSemanticTokens hqlProvider, JpqlSupportState supportState, Optional<SpelSemanticTokens> spelSemanticTokens) {
		this.jpqlProvider = jpqlProvider;
		this.hqlProvider = hqlProvider;
		this.supportState = supportState;
		
		this.sqlTokenProviders = new LinkedHashMap<>();
		this.sqlTokenProviders.put(SqlType.MYSQL, new MySqlSemanticTokens(spelSemanticTokens));
		this.sqlTokenProviders.put(SqlType.POSTGRESQL, new PostgreSqlSemanticTokens(spelSemanticTokens));
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
				Expression queryExpression = null;
				boolean isNative = false;
				if (isQueryAnnotation(a)) {
					for (Object value : a.values()) {
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
				} else if (isNamedQueryAnnotation(a)) {
					for (Object value : a.values()) {
						if (value instanceof MemberValuePair) {
							MemberValuePair pair = (MemberValuePair) value;
							String name = pair.getName().getFullyQualifiedName();
							if (name != null) {
								switch (name) {
								case "query":
									queryExpression = pair.getValue();
									break;
								}
							}
						}
					}
				}
				
				if (queryExpression != null) {
					if (isNative) {
						computeTokensForExpression(getSqlSemanticTokensProvider(jp), queryExpression).forEach(tokensData::accept);
					} else {
						computeTokensForExpression(provider, queryExpression).forEach(tokensData::accept);
					}
				}

				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation a) {
				if (isQueryAnnotation(a)) {
					computeTokensForExpression(provider, a.getValue()).forEach(tokensData::accept);
				}
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				if ("createQuery".equals(node.getName().getIdentifier()) && node.arguments().size() <= 2 && node.arguments().get(0) instanceof Expression queryExpr) {
					IMethodBinding methodBinding = node.resolveMethodBinding();
					if ("jakarta.persistence.EntityManager".equals(methodBinding.getDeclaringClass().getQualifiedName())) {
						if (methodBinding.getParameterTypes().length <= 2 && "java.lang.String".equals(methodBinding.getParameterTypes()[0].getQualifiedName())) {
							computeTokensForExpression(provider, queryExpr).forEach(tokensData::accept);
						}
					}
				}
				return super.visit(node);
			}
		};
	}
	
	public static List<SemanticTokenData> computeTokensForExpression(SemanticTokensDataProvider provider, Expression valueExp) {
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

	
	static boolean isQueryAnnotation(Annotation a) {
		if (Annotations.DATA_QUERY.equals(a.getTypeName().getFullyQualifiedName()) || QUERY.equals(a.getTypeName().getFullyQualifiedName())) {
			ITypeBinding type = a.resolveTypeBinding();
			if (type != null) {
				return AnnotationHierarchies.hasTransitiveSuperAnnotationType(type, Annotations.DATA_QUERY);
			}
		}
		return false;
	}
	
	static boolean isNamedQueryAnnotation(Annotation a) {
		if (NAMED_QUERY.equals(a.getTypeName().getFullyQualifiedName()) || Annotations.JPA_JAKARTA_NAMED_QUERY.equals(a.getTypeName().getFullyQualifiedName())
				|| Annotations.JPA_JAVAX_NAMED_QUERY.equals(a.getTypeName().getFullyQualifiedName())) {
			ITypeBinding type = a.resolveTypeBinding();
			if (type != null) {
				return AnnotationHierarchies.hasTransitiveSuperAnnotationType(type, Annotations.JPA_JAKARTA_NAMED_QUERY)
						|| AnnotationHierarchies.hasTransitiveSuperAnnotationType(type, Annotations.JPA_JAVAX_NAMED_QUERY);
			}
		}
		return false;
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return supportState.isEnabled() && (SpringProjectUtil.hasDependencyStartingWith(project, "spring-data-jpa", null) 
				|| SpringProjectUtil.hasDependencyStartingWith(project, "jakarta.persistence-api", null)
				|| SpringProjectUtil.hasDependencyStartingWith(project, "javax.persistence-api", null));
	}
	
	private SemanticTokensDataProvider getSqlSemanticTokensProvider(IJavaProject project) {
		if (SpringProjectUtil.hasDependencyStartingWith(project, "mysql-connector", null)) {
			return sqlTokenProviders.get(SqlType.MYSQL);
		} else if (SpringProjectUtil.hasDependencyStartingWith(project, "postgresql", null)) {
			return sqlTokenProviders.get(SqlType.POSTGRESQL);
		}
		return sqlTokenProviders.get(SqlType.MYSQL);
	}

}
