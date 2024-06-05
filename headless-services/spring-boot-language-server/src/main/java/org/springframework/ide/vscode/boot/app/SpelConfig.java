/*******************************************************************************
 * Copyright (c) 2024 Broadcom, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Broadcom, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.boot.java.spel.SpelReconciler;
import org.springframework.ide.vscode.boot.java.spel.SpelSemanticTokens;

@Configuration(proxyBeanMethods = false)
public class SpelConfig {
	
	@Bean SpelSemanticTokens spelSemanticTokens() {
		return new SpelSemanticTokens();
	}
	
	@Bean SpelReconciler spelReconciler(BootJavaConfig config) {
		SpelReconciler reconciler = new SpelReconciler();
		config.addListener(v -> reconciler.setEnabled(config.isSpelExpressionValidationEnabled()));
		return reconciler;
	}

}
