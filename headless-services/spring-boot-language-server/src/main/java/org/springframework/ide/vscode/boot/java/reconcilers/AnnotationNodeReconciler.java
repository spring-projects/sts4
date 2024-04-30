/*******************************************************************************
 * Copyright (c) 2023, 2024 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.springframework.ide.vscode.boot.app.BootJavaConfig;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.SpelProblemType;
import org.springframework.ide.vscode.boot.java.handlers.SpelExpressionReconciler;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;

public class AnnotationNodeReconciler implements JdtAstReconciler {
	
	public static final String SPRING_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
	public static final String SPRING_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
	
	public static final String SPRING_EVENT_LISTENER = "org.springframework.context.event.EventListener";
	
	public static final String SPRING_PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
	public static final String SPRING_PRE_FILTER = "org.springframework.security.access.prepost.PreFilter";
	public static final String SPRING_POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
	public static final String SPRING_POST_FILTER= "org.springframework.security.access.prepost.PostFilter";
	
	public static final String SPRING_CONDITIONAL_ON_EXPRESSION = "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression";

	private final SpelExpressionReconciler spelExpressionReconciler;
	private final AnnotationReconciler[] reconcilers;
	
	public AnnotationNodeReconciler(BootJavaConfig config) {
		this.spelExpressionReconciler = new SpelExpressionReconciler();
		this.reconcilers = new AnnotationReconciler[] {

				new AnnotationParamReconciler(Annotations.VALUE, null, "#{", "}", spelExpressionReconciler),
				new AnnotationParamReconciler(Annotations.VALUE, "value", "#{", "}", spelExpressionReconciler),

				new AnnotationParamReconciler(SPRING_CACHEABLE, "key", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_CACHEABLE, "condition", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_CACHEABLE, "unless", "", "", spelExpressionReconciler),

				new AnnotationParamReconciler(SPRING_CACHE_EVICT, "key", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_CACHE_EVICT, "condition", "", "", spelExpressionReconciler),

				new AnnotationParamReconciler(SPRING_EVENT_LISTENER, "condition", "", "", spelExpressionReconciler),
				
				new AnnotationParamReconciler(SPRING_PRE_AUTHORIZE, null, "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_PRE_AUTHORIZE, "value", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_PRE_FILTER, null, "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_PRE_FILTER, "value", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_POST_AUTHORIZE, null, "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_POST_AUTHORIZE, "value", "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_POST_FILTER, null, "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_POST_FILTER, "value", "", "", spelExpressionReconciler),

				new AnnotationParamReconciler(SPRING_CONDITIONAL_ON_EXPRESSION, null, "", "", spelExpressionReconciler),
				new AnnotationParamReconciler(SPRING_CONDITIONAL_ON_EXPRESSION, "value", "", "", spelExpressionReconciler)
				
		};
		config.addListener(evt -> this.spelExpressionReconciler.setEnabled(config.isSpelExpressionValidationEnabled()));
	}
	
	@Override
	public boolean isApplicable(IJavaProject project) {
		return true;
	}

	@Override
	public ProblemType getProblemType() {
		return SpelProblemType.JAVA_SPEL_EXPRESSION_SYNTAX;
	}

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docUri, CompilationUnit cu,
			IProblemCollector problemCollector, boolean isCompleteAst) {
		return new ASTVisitor() {
			
			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					visitAnnotation(project, docUri, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}
			
			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					visitAnnotation(project, docUri, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					visitAnnotation(project, docUri, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}			
			
		};
	}

	private void visitAnnotation(IJavaProject project, URI docUri, Annotation node, IProblemCollector problemCollector) {
		ITypeBinding typeBinding = node.resolveTypeBinding();
		if (typeBinding != null) {
			for (int i = 0; i < reconcilers.length; i++) {
				reconcilers[i].visit(project, docUri, node, typeBinding, problemCollector);
			}
		}
	}

}
