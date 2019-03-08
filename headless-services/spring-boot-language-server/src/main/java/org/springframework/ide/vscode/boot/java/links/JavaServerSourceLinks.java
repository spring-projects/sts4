/*******************************************************************************
 * Copyright (c) 2018, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.links;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.JavaProjectFinder;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;
import org.springframework.ide.vscode.commons.protocol.java.JavaDataParams;

public class JavaServerSourceLinks implements SourceLinks {

	private SimpleLanguageServer server;
	private JavaProjectFinder projectFinder;

	public JavaServerSourceLinks(SimpleLanguageServer server, JavaProjectFinder projectFinder) {
		this.server = server;
		this.projectFinder = projectFinder;
	}

	@Override
	public Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName) {
		StringBuilder bindingKey = new StringBuilder();
		bindingKey.append('L');
		bindingKey.append(fqName.replace('.',  '/'));
		bindingKey.append(';');
		String projectUri = project == null ? null : project.getLocationUri().toString();
		CompletableFuture<Optional<String>> link = server.getClient().javadocHoverLink(new JavaDataParams(projectUri, bindingKey.toString(), true))
				.thenApply(l -> Optional.ofNullable(l));
		try {
			return link.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("", e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkUrlForClasspathResource(String path) {
		return SourceLinks.sourceLinkUrlForClasspathResource(this, projectFinder, path);
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.ofNullable(path).map(p -> p.toUri().toString());
	}

}
