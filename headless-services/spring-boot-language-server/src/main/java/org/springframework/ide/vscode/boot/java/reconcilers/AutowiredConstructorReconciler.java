/*******************************************************************************
 * Copyright (c) 2022 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.reconcilers;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.springframework.ide.vscode.boot.java.Annotations;
import org.springframework.ide.vscode.boot.java.SpringJavaProblemType;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.java.Version;
import org.springframework.ide.vscode.commons.languageserver.quickfix.Quickfix.QuickfixData;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixType;
import org.springframework.ide.vscode.commons.languageserver.reconcile.IProblemCollector;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ReconcileProblemImpl;
import org.springframework.ide.vscode.commons.util.text.IDocument;

public class AutowiredConstructorReconciler implements AnnotationReconciler {

	public static final String REMOVE_UNNECESSARY_AUTOWIRED_FROM_CONSTRUCTOR = "RemoveUnnecessaryConstructorAutowired";
	
	private QuickfixRegistry quickfixRegistry;

	public AutowiredConstructorReconciler(QuickfixRegistry quickfixRegistry) {
		this.quickfixRegistry = quickfixRegistry;
	}

	@Override
	public void visit(IJavaProject project, IDocument doc, Annotation node, ITypeBinding typeBinding, IProblemCollector problemCollector) {
		Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
		
		if (version.getMajor() >= 2) {
			getSingleAutowiredConstructorDeclaringType(node, typeBinding).ifPresent(type -> {
				ReconcileProblemImpl problem = new ReconcileProblemImpl(SpringJavaProblemType.JAVA_AUTOWIRED_CONSTRUCTOR, "Unnecesary @Autowired", node.getStartPosition(), node.getLength());
				QuickfixType quickfixType = quickfixRegistry.getQuickfixType(AutowiredConstructorReconciler.REMOVE_UNNECESSARY_AUTOWIRED_FROM_CONSTRUCTOR);
				if (quickfixType != null) {
					problem.addQuickfix(new QuickfixData<>(
							quickfixType,
							List.of(doc.getUri(), type.getQualifiedName()),
							"Remove unnecessary @Autowired"
					));
				}
				problemCollector.accept(problem);
			});
		}
		
	}
	
	static Optional<ITypeBinding> getSingleAutowiredConstructorDeclaringType(Annotation a, ITypeBinding type) {
		if (type != null && Annotations.AUTOWIRED.equals(type.getQualifiedName())) {
			if (a.getParent() instanceof MethodDeclaration) {
				MethodDeclaration method = (MethodDeclaration) a.getParent();
				IMethodBinding methodBinding = method.resolveBinding();
				if (methodBinding != null) {
					ITypeBinding declaringType = methodBinding.getDeclaringClass();
					if (declaringType != null && isOnlyOneConstructor(declaringType)) {
						return Optional.of(declaringType);
					}
				}
			}
		}
		return Optional.empty();
	}
	
	private static boolean isOnlyOneConstructor(ITypeBinding t) {
		int numberOfConstructors = 0;
		if (!t.isInterface()) {
			for (IMethodBinding m : t.getDeclaredMethods()) {
				if (m.isConstructor()) {
					numberOfConstructors++;
					if (numberOfConstructors > 1) {
						break;
					}
				}
			}
		}
		return numberOfConstructors == 1;
	}


}
