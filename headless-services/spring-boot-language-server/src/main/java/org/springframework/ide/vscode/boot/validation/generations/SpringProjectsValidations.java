/*******************************************************************************
 * Copyright (c) 2020, 2021 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.validation.generations;

import java.io.File;
import java.sql.Date;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.ide.vscode.boot.validation.generations.json.Generation;
import org.springframework.ide.vscode.boot.validation.generations.json.Generations;
import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.java.SpringProjectUtil;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.util.AsyncRunner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import reactor.core.publisher.Mono;

public class SpringProjectsValidations {

	private static final long TIMEOUT_SECS = 30;

	private final List<SpringProjectsProvider> projectsProviders;
	private final SimpleLanguageServer server;

	public SpringProjectsValidations(SimpleLanguageServer server, List<SpringProjectsProvider> projectsProviders) {
		this.projectsProviders = projectsProviders;
		this.server = server;
	}

	public CompletableFuture<List<String>> getValidationMessagesAsync(IJavaProject jp) {
		AsyncRunner async = this.server.getAsync();
		if (async != null) {
			return async.invoke(Duration.ofSeconds(TIMEOUT_SECS), () -> {
				return getWarningMessages(jp);
			});
		} else {
			return Mono.fromCallable(() -> getWarningMessages(jp)).timeout(Duration.ofSeconds(TIMEOUT_SECS))
					.toFuture();
		}
	}

	public List<String> getWarningMessages(IJavaProject jp) throws Exception {
		ImmutableList.Builder<String> messages = ImmutableList.builder();

		if (jp != null) {
			List<File> librariesOnClasspath = SpringProjectUtil.getLibrariesOnClasspath(jp, "spring");
			if (librariesOnClasspath != null) {
				for (File file : librariesOnClasspath) {
					SpringVersionInfo versionInfo = new SpringVersionInfo(file);
					for (SpringProjectsProvider projectsProvider : projectsProviders) {
						Generations generations = projectsProvider.getGenerations(versionInfo.getSlug());
						if (generations != null) {
							List<Generation> gens = generations.getGenerations();
							if (gens != null) {
								for (Generation gen : gens) {
									resolveWarnings(gen, messages, versionInfo);
								}
							}
						}
					}
				}
			}
		}
		return messages.build();
	}

	private void resolveWarnings(Generation gen, Builder<String> messages, SpringVersionInfo versionInfo) {
		if (isInGeneration(versionInfo.getMajMin(), gen)) {
			Date currentDate = new Date(System.currentTimeMillis());
			Date ossEndDate =  Date.valueOf(gen.getOssSupportEndDate());
			Date commercialEndDate = Date.valueOf(gen.getCommercialSupportEndDate());
			
			StringBuilder msg = new StringBuilder();
			
			msg.append("Using ");
			msg.append(versionInfo.getSlug());
			msg.append(" version: ");
			msg.append(versionInfo.getFullVersion());
			
			if (currentDate.after(ossEndDate)) {
				msg.append(" - OSS has ended on: ");
				msg.append(gen.getOssSupportEndDate());
			}
			if (currentDate.after(commercialEndDate)) {
				msg.append(" - Commercial support has ended on: ");
				msg.append(gen.getCommercialSupportEndDate());
			}
	
			messages.add(msg.toString());
		}
	}
	
	private boolean isInGeneration(String version, Generation g) {
		return g.getName().contains(version);
	}
}
