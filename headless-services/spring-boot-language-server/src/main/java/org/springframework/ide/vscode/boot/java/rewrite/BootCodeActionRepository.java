/*******************************************************************************
 * Copyright (c) 2022, 2023 VMware, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.rewrite;

import java.util.List;

import org.springframework.ide.vscode.boot.java.rewrite.reconcile.AddConfigurationIfBeansPresentCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.AuthorizeHttpRequestsCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.AutowiredFieldIntoConstructorParameterCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.BeanMethodNotPublicProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.BeanPostProcessingIgnoreInAotProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.Boot3NotSupportedTypeProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.HttpSecurityLamdaDslCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.NoAutowiredOnConstructorProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.NoRepoAnnotationProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.NoRequestMappingAnnotationCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.NotRegisteredBeansProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.PreciseBeanTypeProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.ServerHttpSecurityLambdaDslCodeAction;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.UnnecessarySpringExtensionProblem;
import org.springframework.ide.vscode.boot.java.rewrite.reconcile.WebSecurityConfigurerAdapterCodeAction;
import org.springframework.ide.vscode.commons.rewrite.config.CodeActionRepository;
import org.springframework.ide.vscode.commons.rewrite.config.RecipeCodeActionDescriptor;

public class BootCodeActionRepository extends CodeActionRepository {

	@Override
	public List<RecipeCodeActionDescriptor> getCodeActionDescriptors() {
		return List.of(
				new BeanMethodNotPublicProblem(),
				new NoAutowiredOnConstructorProblem(),
				new UnnecessarySpringExtensionProblem(),
				new PreciseBeanTypeProblem(),
				new BeanPostProcessingIgnoreInAotProblem(),
				new NotRegisteredBeansProblem(),
				new Boot3NotSupportedTypeProblem(),
				new NoRequestMappingAnnotationCodeAction(),
				new AutowiredFieldIntoConstructorParameterCodeAction(),
				new NoRepoAnnotationProblem(),
				new HttpSecurityLamdaDslCodeAction(),
				new ServerHttpSecurityLambdaDslCodeAction(),
				new AddConfigurationIfBeansPresentCodeAction(),
				new AuthorizeHttpRequestsCodeAction(),
				new WebSecurityConfigurerAdapterCodeAction()
		);
	}

}
