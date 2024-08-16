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

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.boot.java.spel.SpelReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.parser.mysql.MySqlLexer;
import org.springframework.ide.vscode.parser.mysql.MySqlParser;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlLexer;
import org.springframework.ide.vscode.parser.postgresql.PostgreSqlParser;

public class QueryJdtAstReconciler implements JdtAstReconciler {
	
	private final Reconciler hqlReconciler;
	private final Reconciler jpqlReconciler;
	private final Map<SqlType, Reconciler> sqlReconcilers;

	
	public QueryJdtAstReconciler(Reconciler hqlReconciler, Reconciler jpqlReconciler,
			Optional<SpelReconciler> spelReconciler) {
		this.hqlReconciler = hqlReconciler;
		this.jpqlReconciler = jpqlReconciler;
		
		this.sqlReconcilers = new LinkedHashMap<>();
		this.sqlReconcilers.put(SqlType.MYSQL, new AntlrReconcilerWithSpel("MySQL", MySqlParser.class, MySqlLexer.class, "sqlStatements", QueryProblemType.SQL_SYNTAX, spelReconciler, MySqlLexer.SPEL));
		this.sqlReconcilers.put(SqlType.POSTGRESQL, new AntlrReconcilerWithSpel("PostgreSQL", PostgreSqlParser.class, PostgreSqlLexer.class, "root", QueryProblemType.SQL_SYNTAX, spelReconciler, PostgreSqlLexer.SPEL));
	}

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docURI, CompilationUnit cu, IProblemCollector problemCollector, boolean isCompleteAst) throws RequiredCompleteAstException {
		return new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation node) {
				
				Expression queryExpression = null;
				boolean isNative = false;
				if (JdtDataQuerySemanticTokensProvider.isQueryAnnotation(node)) {
					for (Object value : node.values()) {
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
				} else if (JdtDataQuerySemanticTokensProvider.isNamedQueryAnnotation(node)) {
					for (Object value : node.values()) {
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
						final Expression expr = queryExpression;
						getSqlReconciler(project).ifPresent(r -> reconcileExpression(r, expr, problemCollector));
					} else {
						reconcileExpression(getQueryReconciler(project), queryExpression, problemCollector);
					}
				}
				
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				if (JdtDataQuerySemanticTokensProvider.isQueryAnnotation(node)) {
					reconcileExpression(getQueryReconciler(project), node.getValue(), problemCollector);
				}
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				if ("createQuery".equals(node.getName().getIdentifier()) && node.arguments().size() <= 2 && node.arguments().get(0) instanceof Expression queryExpr) {
					IMethodBinding methodBinding = node.resolveMethodBinding();
					if ("jakarta.persistence.EntityManager".equals(methodBinding.getDeclaringClass().getQualifiedName())) {
						if (methodBinding.getParameterTypes().length <= 2 && "java.lang.String".equals(methodBinding.getParameterTypes()[0].getQualifiedName())) {
							reconcileExpression(getQueryReconciler(project), queryExpr, problemCollector);
						}
					}
				}
				return super.visit(node);
			}
			
		};
	}
	
	/*
	 * Gets either HQL or JPQL reconciler
	 */
	private Reconciler getQueryReconciler(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "hibernate-core", null) ? hqlReconciler : jpqlReconciler;
	}
	
	public static void reconcileExpression(Reconciler reconciler, Expression valueExp, IProblemCollector problemCollector) {
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
			reconciler.reconcile(query, offset, problemCollector);
		}
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "spring-data-jpa", null);
	}

	@Override
	public ProblemType getProblemType() {
		return QueryProblemType.JPQL_SYNTAX;
	}
	
	private Optional<Reconciler> getSqlReconciler(IJavaProject project) {
		if (SpringProjectUtil.hasDependencyStartingWith(project, "mysql-connector", null)) {
			return Optional.of(sqlReconcilers.get(SqlType.MYSQL));
		} else if (SpringProjectUtil.hasDependencyStartingWith(project, "postgresql", null)) {
			return Optional.of(sqlReconcilers.get(SqlType.POSTGRESQL));
		}
		return Optional.empty();
	}

}
