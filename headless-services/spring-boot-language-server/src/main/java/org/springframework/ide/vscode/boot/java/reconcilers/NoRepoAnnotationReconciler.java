/*******************************************************************************
 * Copyright (c) 2023, 2025 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.openrewrite.java.spring.NoRepoAnnotationOnRepoInterface;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class NoRepoAnnotationReconciler implements JdtAstReconciler {
	
	private static final String PROBLEM_LABEL = "Unnecessary @Repository";
	private static final String FIX_LABEL = "Remove Unnecessary @Repository";
	private static final String INTERFACE_REPOSITORY = "org.springframework.data.repository.Repository";
	
	private QuickfixRegistry registry;

	public NoRepoAnnotationReconciler(QuickfixRegistry registry) {
		this.registry = registry;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(2, 0, 0).test(project);
	}

	@Override
	public Boot2JavaProblemType getProblemType() {
		return Boot2JavaProblemType.JAVA_REPOSITORY;
	}

	@Override
	public ASTVisitor createVisitor(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector, boolean isCompleteAst, boolean isIndexComplete) {

		return new ASTVisitor() {
			
			@Override
			public boolean visit(TypeDeclaration typeDecl) {
				if (typeDecl.isInterface()) {
					for (Object o : typeDecl.modifiers()) {
						if (o instanceof Annotation) {
							Annotation a = (Annotation) o;
							if (isApplicableRepoAnnotation(a)) {
								ITypeBinding type = typeDecl.resolveBinding();
								if (type != null && isRepo(type)) {
									ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), PROBLEM_LABEL, a.getStartPosition(), a.getLength());
									String uri = docUri.toASCIIString();
									String id = NoRepoAnnotationOnRepoInterface.class.getName();
									ReconcileUtils.setRewriteFixes(registry, problem, List.of(
//										new FixDescriptor(ID, List.of(uri), FIX_LABEL)
//											.withRangeScope(RewriteQuickFixUtils.createOpenRewriteRange(cu, typeDecl))
//											.withRecipeScope(RecipeScope.NODE),
										new FixDescriptor(id, List.of(uri),
												ReconcileUtils.buildLabel(FIX_LABEL, RecipeScope.FILE))
												.withRecipeScope(RecipeScope.FILE),
										new FixDescriptor(id, List.of(uri),
												ReconcileUtils.buildLabel(FIX_LABEL, RecipeScope.PROJECT))
												.withRecipeScope(RecipeScope.PROJECT)
									));
									problemCollector.accept(problem);
								}
							}
						}
					}
				}
				return super.visit(typeDecl);
			}
		};
	}
	
	private static boolean isApplicableRepoAnnotation(Annotation a) {
		if (a instanceof MarkerAnnotation || (a.isNormalAnnotation() && ((NormalAnnotation) a).properties().isEmpty())) {
			String typeName = a.getTypeName().getFullyQualifiedName();
			if (Annotations.REPOSITORY.equals(typeName)) {
				return true;
			} else if (typeName.endsWith("Repository")) {
				ITypeBinding type = a.resolveTypeBinding();
				if (type != null && Annotations.REPOSITORY.equals(type.getQualifiedName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean isRepo(ITypeBinding t) {
		if (INTERFACE_REPOSITORY.equals(t.getQualifiedName())) {
			return true;
		} else {
			for (ITypeBinding st : t.getInterfaces()) {
				if (isRepo(st)) {
					return true;
				}
			}
		}
		return false;
	}

}
