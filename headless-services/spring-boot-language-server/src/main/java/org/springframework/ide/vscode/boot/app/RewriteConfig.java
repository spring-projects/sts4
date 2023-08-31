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
package org.springframework.ide.vscode.boot.app;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.reconcilers.JdtReconciler;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteCodeActionHandler;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRecipeRepository;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.boot.java.rewrite.SpringBootUpgrade;
import org.springframework.ide.vscode.boot.java.utils.CompilationUnitCache;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration(proxyBeanMethods = false)
public class RewriteConfig {

	@ConditionalOnMissingClass("org.springframework.ide.vscode.languageserver.testharness.LanguageServerHarness")
	@Bean RewriteRecipeRepository rewriteRecipesRepository(SimpleLanguageServer server, JavaProjectFinder projectFinder, BootJavaConfig config) {
		return new RewriteRecipeRepository(server, projectFinder, config);
	}
	
	@ConditionalOnBean(RewriteRecipeRepository.class)
	@Bean RewriteRefactorings rewriteRefactorings(SimpleLanguageServer server, JavaProjectFinder projectFinder, RewriteRecipeRepository recipeRepo) {
		return new RewriteRefactorings(server, projectFinder, recipeRepo);
	}
	
	@ConditionalOnBean(RewriteRecipeRepository.class)
	@Bean RewriteCodeActionHandler rewriteCodeActionHandler(CompilationUnitCache cuCache, BootJavaConfig config, JdtReconciler jdtReconciler, SimpleLanguageServer server) {
		return new RewriteCodeActionHandler(cuCache, config, jdtReconciler, server.getQuickfixRegistry());
	}
	
	@ConditionalOnBean(RewriteRecipeRepository.class)
	@Bean SpringBootUpgrade springBootUpgrade(SimpleLanguageServer server, RewriteRecipeRepository recipeRepo, JavaProjectFinder projectFinder) {
		return new SpringBootUpgrade(server, recipeRepo, projectFinder);
	}

}
