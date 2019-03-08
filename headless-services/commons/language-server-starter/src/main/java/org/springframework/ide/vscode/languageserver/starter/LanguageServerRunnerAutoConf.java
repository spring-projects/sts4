/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.languageserver.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ide.vscode.commons.languageserver.LanguageServerRunner;
import org.springframework.ide.vscode.commons.languageserver.config.LanguageServerProperties;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

@Configuration
public class LanguageServerRunnerAutoConf {

	@Bean public LanguageServerRunner serverApp(
			@Qualifier("serverName") String serverName,
			LanguageServerProperties properties, 
			SimpleLanguageServer languageServerFactory
	) {
		return new LanguageServerRunner(serverName, properties, languageServerFactory);
	}

}
