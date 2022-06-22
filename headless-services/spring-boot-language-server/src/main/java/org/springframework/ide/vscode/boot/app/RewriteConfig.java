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
package org.springframework.ide.vscode.boot.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteCodeActionHandler;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteCompilationUnitCache;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRecipeRepository;
import org.springframework.ide.vscode.boot.java.rewrite.RewriteRefactorings;
import org.springframework.ide.vscode.commons.languageserver.quickfix.QuickfixRegistry;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration
public class RewriteConfig implements InitializingBean {

	@Autowired
	private SimpleLanguageServer server;

	@Autowired
	private RewriteRecipeRepository recipeRepo;
	
	@Autowired
	private RewriteRefactorings rewriteRefactorings;
	
	@Bean
	RewriteCodeActionHandler rewriteCodeActionHandler(RewriteCompilationUnitCache cuCache, RewriteRecipeRepository recipeRepo) {
		return new RewriteCodeActionHandler(cuCache, recipeRepo);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		QuickfixRegistry registry = server.getQuickfixRegistry();
		
		recipeRepo.loaded.thenAccept(v ->
			recipeRepo.getProblemRecipeDescriptors().forEach(d -> {
				if (recipeRepo.getRecipe(d.getRecipeId()).isPresent()) {
					registry.register(d.getRecipeId(), rewriteRefactorings);
				}
			})
		);
		
	}

}
