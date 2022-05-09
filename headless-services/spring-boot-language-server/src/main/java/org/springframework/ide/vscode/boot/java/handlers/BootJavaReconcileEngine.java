/*******************************************************************************
 * Copyright (c) 2016, 2022 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.handlers;

import java.net.URI;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.springframework.ide.vscode.boot.java.reconcilers.AnnotationParamReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.AnnotationReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.AutowiredConstructorReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.BeanMethodNotPublicReconciler;
import org.springframework.ide.vscode.boot.java.reconcilers.UnnecessarySpringExtensionReconciler;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.value.Constants;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReconcileEngine implements IReconcileEngine {

	// annotations with SpEL expression params 
	public static final String SPRING_CACHEABLE = "org.springframework.cache.annotation.Cacheable";
	public static final String SPRING_CACHE_EVICT = "org.springframework.cache.annotation.CacheEvict";
	
	public static final String SPRING_EVENT_LISTENER = "org.springframework.context.event.EventListener";
	
	public static final String SPRING_PRE_AUTHORIZE = "org.springframework.security.access.prepost.PreAuthorize";
	public static final String SPRING_PRE_FILTER = "org.springframework.security.access.prepost.PreFilter";
	public static final String SPRING_POST_AUTHORIZE = "org.springframework.security.access.prepost.PostAuthorize";
	public static final String SPRING_POST_FILTER= "org.springframework.security.access.prepost.PostFilter";
	
	public static final String SPRING_CONDITIONAL_ON_EXPRESSION = "org.springframework.boot.autoconfigure.condition.ConditionalOnExpression";
	
	private final JavaProjectFinder projectFinder; 
	private final CompilationUnitCache compilationUnitCache;
	private final AnnotationReconciler[] reconcilers;
	private final SpelExpressionReconciler spelExpressionReconciler;
	private final QuickfixRegistry quickfixRegistry;
	
	public BootJavaReconcileEngine(SimpleLanguageServer server, CompilationUnitCache compilationUnitCache, JavaProjectFinder projectFinder) {
		this.compilationUnitCache = compilationUnitCache;
		this.projectFinder = projectFinder;
		this.quickfixRegistry = server.getQuickfixRegistry();
		
		this.spelExpressionReconciler = new SpelExpressionReconciler();
		
		this.reconcilers = new AnnotationReconciler[] {

				new AnnotationParamReconciler(Constants.SPRING_VALUE, null, "#{", "}", spelExpressionReconciler),
				new AnnotationParamReconciler(Constants.SPRING_VALUE, "value", "#{", "}", spelExpressionReconciler),

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
				new AnnotationParamReconciler(SPRING_CONDITIONAL_ON_EXPRESSION, "value", "", "", spelExpressionReconciler),
				
				new AutowiredConstructorReconciler(quickfixRegistry),
				new BeanMethodNotPublicReconciler(quickfixRegistry),
				new UnnecessarySpringExtensionReconciler()
		};
	}

	public void setSpelExpressionSyntaxValidationEnabled(boolean spelExpressionValidationEnabled) {
		this.spelExpressionReconciler.setEnabled(spelExpressionValidationEnabled);
	}

	@Override
	public void reconcile(final IDocument doc, final IProblemCollector problemCollector) {
		IJavaProject project = projectFinder.find(new TextDocumentIdentifier(doc.getUri())).orElse(null);
		URI uri = URI.create(doc.getUri());
		
		if (project != null) {
			
			try {
				problemCollector.beginCollecting();
				
				compilationUnitCache.withCompilationUnit(project, uri, cu -> {
					if (cu != null) {
						reconcileAST(project, doc, cu, problemCollector);
					}
					
					return null;
				});
			}
			finally {
				problemCollector.endCollecting();
			}
		}
	}

	private void reconcileAST(IJavaProject project, IDocument doc, CompilationUnit cu, IProblemCollector problemCollector) {
		cu.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					visitAnnotation(project, doc, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}
			
			@Override
			public boolean visit(NormalAnnotation node) {
				try {
					visitAnnotation(project, doc, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}

			@Override
			public boolean visit(MarkerAnnotation node) {
				try {
					visitAnnotation(project, doc, node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}			
			
		});
	}

	protected void visitAnnotation(IJavaProject project, IDocument doc, Annotation node, IProblemCollector problemCollector) {
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			for (int i = 0; i < reconcilers.length; i++) {
				reconcilers[i].visit(project, doc, node, typeBinding, problemCollector);
			}
		}
	}

}
