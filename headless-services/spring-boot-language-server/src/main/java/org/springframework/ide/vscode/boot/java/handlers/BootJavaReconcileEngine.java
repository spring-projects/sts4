/*******************************************************************************
 * Copyright (c) 2016, 2020 Pivotal, Inc.
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.boot.java.value.Constants;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IReconcileEngine;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemSeverity;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemTypes;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblem;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.IDocument;

/**
 * @author Martin Lippert
 */
public class BootJavaReconcileEngine implements IReconcileEngine {

	private static final Logger log = LoggerFactory.getLogger(BootJavaReconcileEngine.class);

	private final JavaProjectFinder projectFinder; 
	private final CompilationUnitCache compilationUnitCache;

	private boolean spelExpressionValidationEnabled;
	
	public BootJavaReconcileEngine(CompilationUnitCache compilationUnitCache, JavaProjectFinder projectFinder) {
		this.compilationUnitCache = compilationUnitCache;
		this.projectFinder = projectFinder;
		
		this.spelExpressionValidationEnabled = true;
	}

	public void setSpelExpressionSyntaxValidationEnabled(boolean spelExpressionValidationEnabled) {
		this.spelExpressionValidationEnabled = spelExpressionValidationEnabled;
	}

	@Override
	public void reconcile(final IDocument doc, final IProblemCollector problemCollector) {
		System.out.println("reconcile document: " + doc);
		
		IJavaProject project = projectFinder.find(new TextDocumentIdentifier(doc.getUri())).orElse(null);
		URI uri = URI.create(doc.getUri());
		
		if (project != null) {
			
			try {
				problemCollector.beginCollecting();
				
				compilationUnitCache.withCompilationUnit(project, uri, cu -> {
					if (cu != null) {
						reconcileAST(cu, problemCollector);
					}
					
					return null;
				});
			}
			finally {
				problemCollector.endCollecting();
			}
		}
	}

	private void reconcileAST(CompilationUnit cu, IProblemCollector problemCollector) {
		cu.accept(new ASTVisitor() {
			
			@Override
			public boolean visit(SingleMemberAnnotation node) {
				try {
					visitAnnotation(node, problemCollector);
				}
				catch (Exception e) {
				}
				return super.visit(node);
			}
		});
	}

	protected void visitAnnotation(SingleMemberAnnotation node, IProblemCollector problemCollector) {
		if (!spelExpressionValidationEnabled) {
			return;
		}
		
		ITypeBinding typeBinding = node.resolveTypeBinding();

		if (typeBinding != null) {
			String qname = typeBinding.getQualifiedName();
			if (Constants.SPRING_VALUE.equals(qname)) {
				Expression valueExp = node.getValue();

				if (valueExp instanceof StringLiteral) {
					String value = ((StringLiteral) valueExp).getLiteralValue();
					if (value != null && value.startsWith("#{") && value.endsWith("}")) {
						reconcileSpELExpressionValue(value, valueExp, problemCollector);
					}
				}
			}
		}
	}

	private void reconcileSpELExpressionValue(String value, Expression valueExp, IProblemCollector problemCollector) {
		String spelExpression = value.substring(2, value.length() - 1);
		
		if (spelExpression.length() > 0) {
			SpelExpressionParser parser = new SpelExpressionParser();
			try {
				parser.parseExpression(spelExpression);
			}
			catch (ParseException e) {
				String message = e.getSimpleMessage();
				int position = e.getPosition();
				
				createProblem(valueExp, message, position, problemCollector);
			}
		}
	}

	private void createProblem(Expression valueExp, String message, int position, IProblemCollector problemCollector) {
		int start = valueExp.getStartPosition() + 3 + position;
		int length = valueExp.getLength() - 5 - position;
		
		ReconcileProblem problem = new ReconcileProblemImpl(ProblemTypes.create("SpEL Expression Problem", ProblemSeverity.ERROR), message, start, length);
		problemCollector.accept(problem);
	}

}
