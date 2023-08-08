/*******************************************************************************
 * Copyright (c) 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.openrewrite.java.spring.boot2.AddConfigurationAnnotationIfBeansPresent;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.Boot2JavaProblemType;
import org.springframework.ide.vscode.boot.java.annotations.AnnotationHierarchies;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeScope;
import org.springframework.ide.vscode.commons.rewrite.java.FixDescriptor;

public class AddConfigurationIfBeansPresentReconciler implements JdtAstReconciler {

	private static final String ID = AddConfigurationAnnotationIfBeansPresent.class.getName();

	private static final String PROBLEM_LABEL = "'@Configuration' is missing on a class defining Spring Beans";

	private static final String FIX_LABEL = "Add missing '@Configuration' annotations over classes";

	private QuickfixRegistry quickfixRegistry;

	public AddConfigurationIfBeansPresentReconciler(QuickfixRegistry quickfixRegistry) {
		this.quickfixRegistry = quickfixRegistry;
	}

	@Override
	public void reconcile(IJavaProject project, URI docUri, CompilationUnit cu, IProblemCollector problemCollector,
			boolean isCompleteAst) {
		cu.accept(new ASTVisitor() {

			@Override
			public boolean visit(TypeDeclaration classDecl) {
				if (isApplicableClass(cu, classDecl)) {

					SimpleName nameAst = classDecl.getName();
					ReconcileProblemImpl problem = new ReconcileProblemImpl(getProblemType(), PROBLEM_LABEL,
							nameAst.getStartPosition(), nameAst.getLength());

					RewriteQuickFixUtils.setRewriteFixes(quickfixRegistry, problem,
							List.of(new FixDescriptor(ID, List.of(docUri.toASCIIString()),
									RewriteQuickFixUtils.buildLabel(FIX_LABEL, RecipeScope.FILE))
									.withRecipeScope(RecipeScope.FILE),
									new FixDescriptor(ID, List.of(docUri.toASCIIString()),
											RewriteQuickFixUtils.buildLabel(FIX_LABEL, RecipeScope.PROJECT))
											.withRecipeScope(RecipeScope.PROJECT)));

					problemCollector.accept(problem);
				}
				return true;
			}

		});
	}

	private static boolean isApplicableClass(CompilationUnit cu, TypeDeclaration classDecl) {
		if (classDecl.isInterface()) {
			return false;
		}

		if (Modifier.isAbstract(classDecl.getModifiers())) {
			return false;
		}

		boolean isStatic = Modifier.isStatic(classDecl.getModifiers());

		if (!isStatic) {
			// no static keyword? check if it is top level class in the CU

			for (ASTNode p = classDecl.getParent(); p != cu && p != null; p = p.getParent()) {
				if (p instanceof TypeDeclaration) {
					return false;
				}
			}
		}

		// check if '@Configuration' is already over the class
		for (Iterator<?> itr = classDecl.modifiers().iterator(); itr.hasNext();) {
			Object mod = itr.next();
			if (mod instanceof Annotation) {
				Annotation a = (Annotation) mod;
				ITypeBinding aType = a.resolveTypeBinding();
				if (aType != null && AnnotationHierarchies.isSubtypeOf(a, Annotations.CONFIGURATION)) {
					// Found '@Configuration' annotation
					return false;
				}
			}
		}

		// No '@Configuration' present. Check if any methods have '@Bean' annotation
		for (MethodDeclaration m : classDecl.getMethods()) {
			if (isBeanMethod(m)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isBeanMethod(MethodDeclaration m) {
		for (Iterator<?> itr = m.modifiers().iterator(); itr.hasNext();) {
			Object mod = itr.next();
			if (mod instanceof Annotation) {
				Annotation a = (Annotation) mod;
				ITypeBinding aType = a.resolveTypeBinding();
				if (aType != null && AnnotationHierarchies.isSubtypeOf(a, Annotations.BEAN)) {
					// Found '@Bean' annotation
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		Version version = SpringProjectUtil.getDependencyVersion(project, "spring-context");
		return version != null && version.compareTo(new Version(3, 0, 0, null)) >= 0;
	}

	@Override
	public ProblemType getProblemType() {
		return Boot2JavaProblemType.MISSING_CONFIGURATION_ANNOTATION;
	}

}
