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
import java.util.List;
import java.util.Set;

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
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.boot.java.handlers.Reconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtAstReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.RequiredCompleteAstException;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public class QueryJdtAstReconciler implements JdtAstReconciler {

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docURI, CompilationUnit cu, IProblemCollector problemCollector, boolean isCompleteAst) throws RequiredCompleteAstException {
		return new ASTVisitor() {

			@Override
			public boolean visit(NormalAnnotation node) {
				
				Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(node.resolveTypeBinding());
				if (!allAnnotations.contains(Annotations.DATA_QUERY)) {
					return false;
				}
				
				List<?> values = node.values();
				
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
						//TODO: SQL syntax validation
					} else {
						reconcileExpression(getQueryReconciler(project), queryExpression, problemCollector);
					}
				}
				
				return false;
			}

			@Override
			public boolean visit(SingleMemberAnnotation node) {
				Set<String> allAnnotations = AnnotationHierarchies.getTransitiveSuperAnnotations(node.resolveTypeBinding());
				if (!allAnnotations.contains(Annotations.DATA_QUERY)) {
					return false;
				}
				reconcileExpression(getQueryReconciler(project), node.getValue(), problemCollector);
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
	private static Reconciler getQueryReconciler(IJavaProject project) {
		return SpringProjectUtil.hasDependencyStartingWith(project, "hibernate-core", null) ? new HqlReconciler() : new JpqlReconciler();
	}
	
	private void reconcileExpression(Reconciler reconciler, Expression valueExp, IProblemCollector problemCollector) {
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
		return QueryProblemType.EXPRESSION_SYNTAX;
	}

}
