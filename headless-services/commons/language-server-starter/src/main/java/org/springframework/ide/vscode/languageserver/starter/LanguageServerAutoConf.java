/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.languageserver.starter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerInitializer;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerProperties;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration
@EnableConfigurationProperties(LanguageServerProperties.class)
public class LanguageServerAutoConf {
	
	@ConditionalOnMissingBean
	@Bean public SimpleLanguageServer languageServer(LanguageServerProperties props, LanguageServerInitializer initializer) throws Exception {
		SimpleLanguageServer server = new SimpleLanguageServer(props.getExtensionId());
		initializer.initialize(server);
		return server;
	}
	
}
