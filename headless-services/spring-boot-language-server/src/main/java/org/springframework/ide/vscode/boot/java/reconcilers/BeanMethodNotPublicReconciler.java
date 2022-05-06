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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

public class BeanMethodNotPublicReconciler implements AnnotationReconciler {
		
	private static final Logger log = LoggerFactory.getLogger(BeanMethodNotPublicReconciler.class);
	
	public static final String REMOVE_PUBLIC_FROM_BEAN_METHOD = "RemovePublicFromBeanMethod";

	private QuickfixRegistry quickfixRegistry;

	public BeanMethodNotPublicReconciler(QuickfixRegistry quickfixRegistry) {
		this.quickfixRegistry = quickfixRegistry;
	}

	@Override
	public void visit(IJavaProject project, IDocument doc, Annotation node, ITypeBinding typeBinding,
			IProblemCollector problemCollector) {
		
		if (Annotations.BEAN.equals(typeBinding.getQualifiedName()) && node.getParent() instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) node.getParent();
			Version version = SpringProjectUtil.getDependencyVersion(project, SpringProjectUtil.SPRING_BOOT);
			if (version.getMajor() >= 2) {
				IMethodBinding methodBinding = m.resolveBinding();
				if (isNotOverridingPublicMethod(methodBinding)) {
					
					ReconcileProblemImpl problem = ((List<?>)m.modifiers()).stream()
							.filter(Modifier.class::isInstance)
							.map(Modifier.class::cast)
							.filter(modifier -> modifier.isPublic())
							.findFirst()
							.map(modifier -> new ReconcileProblemImpl(
									SpringJavaProblemType.JAVA_PUBLIC_BEAN_METHOD, "public @Bean method",
									modifier.getStartPosition(), modifier.getLength()))
							.orElse(new ReconcileProblemImpl(
									SpringJavaProblemType.JAVA_PUBLIC_BEAN_METHOD, "public @Bean method",
									m.getName().getStartPosition(), m.getName().getLength()));
					
					QuickfixType quickfixType = quickfixRegistry.getQuickfixType(REMOVE_PUBLIC_FROM_BEAN_METHOD);
					if (quickfixType != null) {
						problem.addQuickfix(new QuickfixData<>(
								quickfixType,
								createParameters(doc, methodBinding),
								"Remove public from @Bean method"
						));
					}			
					problemCollector.accept(problem);
				}
			}
		}

	}
	
	private static final boolean isOverriding(IMethodBinding binding) {
		try {
			Field f = binding.getClass().getDeclaredField("binding");
			f.setAccessible(true);
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding  value = (org.eclipse.jdt.internal.compiler.lookup.MethodBinding) f.get(binding);
			return value.isOverriding();
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
	
	public static final boolean isNotOverridingPublicMethod(IMethodBinding methodBinding) {
		return !isOverriding(methodBinding) && (methodBinding.getModifiers() & Modifier.PUBLIC) != 0;
	}
	
	public static final List<String> createParameters(IDocument doc, IMethodBinding methodBinding) {
		StringBuilder methodPattern = new StringBuilder(methodBinding.getDeclaringClass().getQualifiedName());
		methodPattern.append(' ');
		methodPattern.append(methodBinding.getName());
		methodPattern.append('(');
		methodPattern.append(Arrays.stream(methodBinding.getParameterTypes()).map(p -> p.getErasure().getQualifiedName()).collect(Collectors.joining(",")));
		methodPattern.append(')');
		return List.of(doc.getUri(), methodPattern.toString());
	}

}
