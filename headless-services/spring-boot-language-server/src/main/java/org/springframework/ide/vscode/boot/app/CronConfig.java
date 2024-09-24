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
import org.springframework.ide.vscode.boot.java.cron.CronReconciler;
import org.springframework.ide.vscode.boot.java.cron.CronSemanticTokens;

@Configuration(proxyBeanMethods = false)
public class CronConfig {
	
	@Bean
	CronSemanticTokens cronSemanticTokens() {
		return new CronSemanticTokens();
	}
	
	@Bean
	CronReconciler cronReconciler() {
		return new CronReconciler();
	}

}
