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
import org.springframework.ide.vscode.boot.java.data.jpa.queries.HqlReconciler;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.HqlSemanticTokens;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JpqlReconciler;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JpqlSemanticTokens;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.JpqlSupportState;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.SqlReconciler;
import org.springframework.ide.vscode.boot.java.data.jpa.queries.SqlSemanticTokens;
import org.springframework.ide.vscode.commons.languageserver.java.ProjectObserver;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration(proxyBeanMethods = false)
public class SpringDataConfig {
	
	@Bean
	JpqlSemanticTokens jpqlSemanticTokens() {
		return new JpqlSemanticTokens();
	}
	
	@Bean
	HqlSemanticTokens hqlSemanticTokens() {
		return new HqlSemanticTokens();
	}
	
	@Bean
	SqlSemanticTokens sqlSemanticTokens() {
		return new SqlSemanticTokens();
	}
	
	@Bean
	HqlReconciler hqlReconciler() {
		return new HqlReconciler();
	}
	
	@Bean
	JpqlReconciler jpqlReconciler() {
		return new JpqlReconciler();
	}
	
	@Bean
	SqlReconciler sqlReconciler() {
		return new SqlReconciler();
	}

	@Bean
	JpqlSupportState jpqlSupportState(SimpleLanguageServer server, ProjectObserver projectObserver, BootJavaConfig config) {
		return new JpqlSupportState(server, projectObserver, config);
	}
	
}
