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
package org.springframework.ide.vscode.boot.java.links;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.ide.vscode.commons.java.IJavaProject;
import org.springframework.ide.vscode.commons.languageserver.java.ls.JavaDataParams;
import org.springframework.ide.vscode.commons.languageserver.util.SimpleLanguageServer;

public class JavaServerSourceLinks implements SourceLinks {

	private SimpleLanguageServer server;

	public JavaServerSourceLinks(SimpleLanguageServer server) {
		this.server = server;
	}

	@Override
	public Optional<String> sourceLinkUrlForFQName(IJavaProject project, String fqName) {
		StringBuilder bindingKey = new StringBuilder();
		bindingKey.append('L');
		bindingKey.append(fqName.replace('.',  '/'));
		bindingKey.append(';');
		CompletableFuture<Optional<String>> link = server.getClient().javadocHoverLink(new JavaDataParams(project.getLocationUri().toString(), bindingKey.toString()))
				.thenApply(response -> Optional.ofNullable(response.getLink()));
		try {
			return link.get(10, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			log.error("", e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkUrlForClasspathResource(IJavaProject project, String path) {
		int idx = path.lastIndexOf(CLASS);
		if (idx >= 0) {
			Path p = Paths.get(path.substring(0, idx));
			return sourceLinkUrlForFQName(project, p.toString().replace(File.separator, "."));
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> sourceLinkForResourcePath(Path path) {
		return Optional.ofNullable(path).map(p -> p.toUri().toString());
	}

}
