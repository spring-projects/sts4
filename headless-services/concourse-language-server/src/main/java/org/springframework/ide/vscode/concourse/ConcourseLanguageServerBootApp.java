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
package org.springframework.ide.vscode.concourse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.ide.vscode.commons.util.LogRedirect;
import org.springframework.ide.vscode.concourse.github.DefaultGithubInfoProvider;
import org.springframework.ide.vscode.concourse.github.GithubInfoProvider;

@SpringBootApplication
public class ConcourseLanguageServerBootApp {

	private static final String SERVER_NAME = "concourse-language-server";

	public static void main(String[] args) throws Exception {
		LogRedirect.bootRedirectToFile(SERVER_NAME); //TODO: use boot (or logback realy) to configure logging instead.
		SpringApplication.run(ConcourseLanguageServerBootApp.class, args);
	}

	@Bean public String serverName() {
		return SERVER_NAME;
	}

	@Bean ConcourseLanguageServerInitializer languageServer(GithubInfoProvider github) {
		return new ConcourseLanguageServerInitializer(github);
	}

	@Bean GithubInfoProvider github() {
		return new DefaultGithubInfoProvider();
	}
}
