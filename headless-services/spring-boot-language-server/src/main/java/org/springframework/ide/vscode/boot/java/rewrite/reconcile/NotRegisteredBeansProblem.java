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
package org.springframework.ide.vscode.boot.java.rewrite.reconcile;

import static org.springframework.ide.vscode.commons.java.SpringProjectUtil.springBootVersionGreaterOrEqual;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.WorkspaceSymbol;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Tree;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.tree.J.ClassDeclaration;
import org.openrewrite.java.tree.JavaType.FullyQualified;
import org.openrewrite.java.tree.TypeUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ide.vscode.boot.app.SpringSymbolIndex;
import org.springframework.ide.vscode.boot.java.Boot3JavaProblemType;
import org.springframework.ide.vscode.boot.java.beans.BeansSymbolAddOnInformation;
import org.springframework.ide.vscode.boot.java.handlers.SymbolAddOnInformation;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.reconcile.ProblemType;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeSpringJavaProblemDescriptor;
import org.springframework.ide.vscode.commons.rewrite.java.FixAssistMarker;

public class NotRegisteredBeansProblem implements RecipeSpringJavaProblemDescriptor {
		
	private static final List<String> AOT_BEANS = List.of(
			"org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor",
			"org.springframework.beans.factory.aot.BeanRegistrationAotProcessor",
			"org.springframework.beans.factory.aot.RuntimeHintsRegistrar"
	);

	@Override
	public JavaVisitor<ExecutionContext> getMarkerVisitor(ApplicationContext applicationContext) {
		return new JavaIsoVisitor<ExecutionContext>() {
			
			@Override
			public ClassDeclaration visitClassDeclaration(ClassDeclaration classDecl, ExecutionContext p) {
				ClassDeclaration c = super.visitClassDeclaration(classDecl, p);
				FullyQualified type = c.getType();
				if (type != null) {
					String beanClassName = type.getFullyQualifiedName();
					boolean applicable = AOT_BEANS.stream().filter(fqName -> TypeUtils.isAssignableTo(fqName, type)).findFirst().isPresent();
					if (applicable) {
						SpringSymbolIndex index = applicationContext.getBean(SpringSymbolIndex.class);
						List<WorkspaceSymbol> beanSymbols = index.getSymbols(data -> {
							SymbolAddOnInformation[] additionalInformation = data.getAdditionalInformation();
							if (additionalInformation != null) {
								for (SymbolAddOnInformation info : additionalInformation) {
									if (info instanceof BeansSymbolAddOnInformation) {
										BeansSymbolAddOnInformation info2 = (BeansSymbolAddOnInformation) info;
										return beanClassName.equals(info2.getBeanType()); 
									}
								}
							}
							return false;
						}).limit(1).collect(Collectors.toList());
						if (beanSymbols.isEmpty()) {
							return c = c.withName(c.getName().withMarkers(c.getName().getMarkers().add(new FixAssistMarker(Tree.randomId(), getId())))); 
						}
					}
				}
				return c;
			}
			
		};
	}

	@Override
	public boolean isApplicable(IJavaProject project) {
		return springBootVersionGreaterOrEqual(3, 0, 0).test(project);
	}

	@Override
	public ProblemType getProblemType() {
		return Boot3JavaProblemType.JAVA_BEAN_NOT_REGISTERED_IN_AOT;
	}

}
